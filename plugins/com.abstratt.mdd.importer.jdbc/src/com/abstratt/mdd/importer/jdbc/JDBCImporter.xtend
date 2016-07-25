package com.abstratt.mdd.importer.jdbc

import com.abstratt.mdd.core.IRepository
import com.abstratt.mdd.frontend.textuml.core.TextUMLConstants
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager
import java.util.Collection
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.Properties
import org.apache.commons.io.FileUtils
import schemacrawler.schema.Catalog
import schemacrawler.schema.Column
import schemacrawler.schema.ColumnDataType
import schemacrawler.schema.ForeignKey
import schemacrawler.schema.Schema
import schemacrawler.schema.Table
import schemacrawler.schemacrawler.SchemaCrawlerOptions
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder
import schemacrawler.tools.integration.serialization.XmlSerializedCatalog
import schemacrawler.utility.SchemaCrawlerUtility

import static extension org.apache.commons.lang.StringUtils.*

class JDBCImporter {

	Map<String, String> specialColumns
	Map<String, String> constantColumns
	Map<String, String> tableRenames
	String selectedSchemaName
	List<String> fragments

	new(Properties properties) {
		this.specialColumns = filterProperties(properties, 'mdd.importer.jdbc.specialColumns.')
		this.constantColumns = filterProperties(properties, 'mdd.importer.jdbc.constantColumns.')
		this.tableRenames = filterProperties(properties, 'mdd.importer.jdbc.table.rename.')
		this.fragments = properties.getOrDefault("mdd.importer.jdbc.table.fixcase.fragments", "").toString().split(',').filter[!blank].map[trim].
			toList
		this.selectedSchemaName = properties.get("mdd.importer.jdbc.schema") as String
		println(this.constantColumns.keySet.join('\n'))
	}

	private def Map<String, String> filterProperties(Properties properties, String prefix) {
		val result = newLinkedHashMap()
		val keys = properties.keySet().map[toString].filter[startsWith(prefix)]
		keys.forEach [
			result.put(it.replace(prefix, ''), properties.get(it).toString())
		]
		return result
	}

	def void importApplication(IRepository repository) {
		val properties = repository.properties
		val connectionUrl = properties.get("mdd.importer.jdbc.connectionUrl") as String
		val username = properties.get("mdd.importer.jdbc.username") as String
		val password = properties.get("mdd.importer.jdbc.password") as String
		val connection = DriverManager.getConnection(connectionUrl, username, password)
		connection.readOnly = true

		try {
			importApplication(repository, connection, properties)
		} finally {
			connection.close()
		}
	}

	def void importApplication(IRepository repository, Connection connection, Properties properties) {
		val options = new SchemaCrawlerOptions
		options.schemaInfoLevel = SchemaInfoLevelBuilder.standard()
		val catalog = SchemaCrawlerUtility.getCatalog(connection, options)
		val schemas = catalog.schemas.filter[selectedSchemaName == null || selectedSchemaName == it.name] 
		schemas.forEach [ schema |
			catalog.getTables(schema).forEach [ table |
				println("o--> " + table)
				println("pk--> " + table.primaryKey)
				println("fk--> " + table.foreignKeys.map [
					it.columnReferences.map[it.foreignKeyColumn + "=>" + it.primaryKeyColumn].join(", ")
				])
				table.columns.forEach [ column |
					println("column: " + column)
					if (column.isPartOfForeignKey) {
					}

				]
			]
		]
	}
	
	def Map<String, CharSequence> importModelFromSnapshot(Reader snapshotContents) {
		val options = new SchemaCrawlerOptions
		options.schemaInfoLevel = SchemaInfoLevelBuilder.standard()
		
		val previousClassloader = Thread.currentThread.contextClassLoader
		Thread.currentThread.contextClassLoader = class.classLoader
		var XmlSerializedCatalog catalog
		try {
			catalog = new XmlSerializedCatalog(snapshotContents)
		} finally {
    		Thread.currentThread.contextClassLoader = previousClassloader
		}
	    return importModel(catalog)
    }
	
	def Map<String, CharSequence> importModelFromSnapshot(File snapshotFile) {
		val asJarURL = URI.create("jar:" + snapshotFile.toURI)
		val zipContents = FileSystems.newFileSystem(asJarURL, Collections.emptyMap)
		try {
			val entry = zipContents.getPath("schemacrawler.data")
			val fileContents = Files.newBufferedReader(entry)
			try {
				return importModelFromSnapshot(fileContents)	
		    } finally {
		    	fileContents.close()
		    }
	    } finally {
	    	zipContents.close()
	    }
	}

	def Map<String, CharSequence> importModel(Catalog catalog) {
		val generated = newLinkedHashMap()
		val schemas = catalog.schemas.filter[selectedSchemaName == null || selectedSchemaName == it.name] 
		schemas.forEach [ schema |
			val tables = catalog.getTables(schema)
			if (!tables.empty)
				generated.put(toPackageName(schema.name), generatePackage(catalog, schema, tables))
		]
		return generated
	}
	
	def String toPackageName(String schemaName) {
		fromSchemaToModel(schemaName).toLowerCase
	}

	def CharSequence generatePackage(Catalog catalog, Schema schema, Collection<Table> tables) {
		val classTables = tables.filter[it.columns.exists[!it.partOfForeignKey]]
		val associationTables = tables.filter[!it.columns.exists[!it.partOfForeignKey]]
		'''
			package «schema.name.toPackageName»;
			
			import mdd_types;
			
			«classTables.map[table | generateClass(catalog, schema, table)].join()»
			«associationTables.map[table | generateAssociation(catalog, schema, table)].join()»
			
			end.
		'''
	}

	def CharSequence generateClass(Catalog catalog, Schema schema, Table table) {
		val ordinaryColumns = table.columns.filter[!partOfForeignKey && !partOfPrimaryKey]
		val foreignKeys = table.foreignKeys.filter[it.columnReferences.exists[it.foreignKeyColumn.parent == table]]

		'''
			class «table.toClassName()»
				«ordinaryColumns.map[column | generateAttribute(catalog, schema, table, column)].join()»
				«foreignKeys.map[fk | generateReference(catalog, schema, table, fk)].join()»
			end;
		'''
	}

	def CharSequence generateAssociation(Catalog catalog, Schema schema, Table table) {
		val foreignKeys = table.foreignKeys.filter[it.columnReferences.exists[it.foreignKeyColumn.parent == table]]

		'''
			association «table.toClassName()»
				«foreignKeys.map[fk | generateAssociationEnd(catalog, schema, table, fk)].join()»
			end;
		'''
	}

	def CharSequence generateAssociationEnd(Catalog catalog, Schema schema, Table table, ForeignKey key) {
		val columnReference = key.columnReferences.
			head
		// «fk.columnReferences.join('\\n')»
		'''
			navigable role «columnReference.foreignKeyColumn.toReferenceName» : «columnReference.primaryKeyColumn.parent.toClassName»«columnReference.foreignKeyColumn.generateMultiplicity»;
		'''
	}

	def CharSequence toClassName(Table table) {
		return toClassName(table.name)
	}
	
	def CharSequence toClassName(String originalTableName) {
		val tableName = (tableRenames.get(originalTableName) ?: originalTableName)
		val className = fromSchemaToModel(tableName)
		if (TextUMLConstants.KEYWORDS.contains(className))
			return ('\\' + className)
		return className
	}

	def fromSchemaToModel(String originalSchemaName) {
		val schemaName = originalSchemaName.toLowerCase.replaceAll(" ", "_").removeStart("\"").removeEnd("\"")
		val modelNameBuffer = new StringBuffer()
		while (modelNameBuffer.length < schemaName.length && !fragments.empty && fragments.exists [ 
			schemaName.startsWith(it, modelNameBuffer.length)
		]) {
			modelNameBuffer.append(fragments.findFirst[schemaName.startsWith(it, modelNameBuffer.length)].toFirstUpper)
		}
		val modelName = modelNameBuffer +
			schemaName.substring(modelNameBuffer.length).split('_').map[toFirstUpper].join()
		return modelName
	}

	def CharSequence toReferenceName(Column fkColumn) {
		val fkColumnName = fkColumn.name
		var referenceName = fkColumnName
		if (fkColumnName.endsWith('id')) {
			referenceName = fkColumnName.substring(0, fkColumnName.length - 2)
		}
		referenceName = fromSchemaToModel(referenceName).toFirstLower
		if (TextUMLConstants.KEYWORDS.contains(referenceName))
			return ('\\' + referenceName)
		return referenceName
	}

	def CharSequence toAttributeName(Column column) {
		val attributeName = fromSchemaToModel(column.name).toLowerCase
		if (TextUMLConstants.KEYWORDS.contains(attributeName))
			return ('\\' + attributeName)
		return attributeName
	}

	def CharSequence generateAttribute(Catalog catalog, Schema schema, Table table, Column column) {
		val modifiers = getAttributeModifiers(column).map['''«it» '''].
			join()
		// «column.name» «column.columnDataType» («column.columnDataType.javaSqlType.javaSqlTypeGroup»)
		'''
			«modifiers»attribute «column.toAttributeName» : «generateAttributeType(column.columnDataType)»«column.generateMultiplicity»«column.generateDefaultValue»;
		'''
	}

	def CharSequence generateDefaultValue(Column column) {
		if (column.constantColumn) {
			''' := «constantColumns.get(column.shortName) ?: constantColumns.get(column.name)»'''
		} else
			''
	}

	def CharSequence generateMultiplicity(Column column) {
		if (column.nullable)
			'[0,1]'
		else
			''
	}

	def CharSequence generateReference(Catalog catalog, Schema schema, Table table, ForeignKey fk) {
		val columnReference = fk.columnReferences.
			head
		// «fk.columnReferences.join('\\n')»
		'''
			reference «columnReference.foreignKeyColumn.toReferenceName» : «columnReference.primaryKeyColumn.parent.toClassName»«columnReference.foreignKeyColumn.generateMultiplicity»;
		'''
	}

	def boolean isSpecialColumn(Column toCheck) {
		return specialColumns.containsKey(toCheck.name)
	}

	def boolean isConstantColumn(Column toCheck) {
		val isConstant = constantColumns.containsKey(toCheck.name) || constantColumns.containsKey(toCheck.shortName)
		return isConstant
	}

	def String gerConstantColumnValue(Column toCheck) {
		return constantColumns.get(toCheck.shortName) ?: constantColumns.get(toCheck.name)
	}

	def List<String> getAttributeModifiers(Column column) {
		val modifiers = newLinkedList()
		if (column.specialColumn)
			modifiers.add("readonly")
		if (column.specialColumn)
			modifiers.add("private")
		if (column.constantColumn)
			modifiers.add("readonly")
		if (column.partOfUniqueIndex && !column.nullable)
			modifiers.add("id")
		return modifiers
	}

	def CharSequence generateAttributeType(ColumnDataType columnType) {
		switch (columnType.javaSqlType.javaSqlTypeGroup) {
			case bit: 'Boolean'
			case character: 'String'
			case integer: 'Integer'
			case real: 'Double'
			case temporal: 'Date'
			case large_object: 'Blob'
			case binary: 'Blob'
			default: 'any'
		}
	}

	def static void main(String[] args) {
		importSnapshot()
	}
	
	def static importSnapshot() {
		
		val properties = new Properties()
		properties.put('mdd.importer.jdbc.table.fixcase.fragments',
			'')
		
		val importer = new JDBCImporter(properties)
		// importer.importApplication(null, connection, null)
		
		val options = new SchemaCrawlerOptions
		options.schemaInfoLevel = SchemaInfoLevelBuilder.standard()
		val catalog = new XmlSerializedCatalog(new InputStreamReader(new FileInputStream("schemacrawler.data")))
		
		importer.importModel(catalog).forEach [ p1, p2 |
			println("key: " + p1)
			println(p2)
		]
	}
	
	
	def static importPostgresDB() {
		val connectionUrl = "jdbc:postgresql://localhost/objectTestDB"
		val username = "proteo"
		val password = ""
		val connection = DriverManager.getConnection(connectionUrl, username, password)
		
		val properties = new Properties()
		properties.put('mdd.importer.jdbc.specialTables.user', 'principals')
		properties.put('mdd.importer.jdbc.constantColumns.sample.name', '"Sample Name"')
		properties.put('mdd.importer.jdbc.constantColumns.datastoreid', '1')
		properties.put('mdd.importer.jdbc.constantColumns.isglobal', 'true')
		properties.put('mdd.importer.jdbc.specialColumns.owner', 'ownerid')
		properties.put('mdd.importer.jdbc.specialColumns.creatorColumn', 'createdby')
		properties.put('mdd.importer.jdbc.specialColumns.updaterColumn', 'lastmodifiedby')
		properties.put('mdd.importer.jdbc.specialColumns.createdColumn', 'createddate')
		properties.put('mdd.importer.jdbc.specialColumns.updatedColumn', 'lastmodifieddate')
		properties.put('mdd.importer.jdbc.table.rename.principals', 'user')
		properties.put('mdd.importer.jdbc.table.rename.processinputtype', 'ProcessInputType')
		properties.put('mdd.importer.jdbc.table.rename.labprotocol', 'Protocol')
		properties.put('mdd.importer.jdbc.table.rename.protocol', 'ProcessTemplate')
		properties.put('mdd.importer.jdbc.table.fixcase', '')
		properties.put('mdd.importer.jdbc.table.fixcase.fragments',
			'volume,vol,udf,unit,process,output,input,control,instrument,external,program,status,type,label,lab,reagent,sample,artifact,protocol,protein,hit,container,workflow,routing,result,step,stage,spot,search,security,special,status,user,common,taskable,task,batch,gel,udf,udt,transfer,addition,add')
		
		val importer = new JDBCImporter(properties)
		// importer.importApplication(null, connection, null)
		
		val options = new SchemaCrawlerOptions
		options.schemaInfoLevel = SchemaInfoLevelBuilder.standard()
		val catalog = SchemaCrawlerUtility.getCatalog(connection, options)
		val snapshotFile = new File("offline.db")
		val snapshot = new OutputStreamWriter(FileUtils.openOutputStream(snapshotFile))
		new XmlSerializedCatalog(catalog).save(snapshot)
		snapshot.close()
		println("Snapshot saved to " + snapshotFile.absolutePath)
		
		importer.importModel(catalog).forEach [ p1, p2 |
			println("key: " + p1)
			println(p2)
		]
	}
}
