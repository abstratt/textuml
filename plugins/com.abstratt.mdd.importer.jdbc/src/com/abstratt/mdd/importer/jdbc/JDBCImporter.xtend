package com.abstratt.mdd.importer.jdbc

import com.abstratt.mdd.core.IRepository
import com.abstratt.mdd.frontend.textuml.core.TextUMLConstants
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.StringWriter
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.sql.Connection
import java.sql.DriverManager
import java.util.Collections
import java.util.List
import java.util.Map
import java.util.Properties
import java.util.Set
import java.util.regex.Pattern
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
import com.abstratt.mdd.core.ParsedProperties
import schemacrawler.schema.DescribedObject

class JDBCImporter {

	Map<String, String> specialColumns
	Map<String, String> constantColumns
	Map<String, String> tableRenames
	Map<String, String> schemaRenames
	String selectedSchemaName
	String selectedCatalogName
	List<String> fragments
	List<Pattern> inclusionPatterns
	List<Pattern> exclusionPatterns
	
	Properties jpaProperties

	new(Properties properties) {
		this.specialColumns = filterProperties(properties, 'mdd.importer.jdbc.specialColumns.')
		this.constantColumns = filterProperties(properties, 'mdd.importer.jdbc.constantColumns.')
		this.tableRenames = filterProperties(properties, 'mdd.importer.jdbc.table.rename.')
		this.schemaRenames = filterProperties(properties, 'mdd.importer.jdbc.schema.rename.')
		this.fragments = properties.getOrDefault("mdd.importer.jdbc.table.fixcase.fragments", "").toString().split(',').filter[!blank].map[trim].
			toList
		this.selectedSchemaName = properties.get("mdd.importer.jdbc.schema") as String
		this.selectedCatalogName = properties.get("mdd.importer.jdbc.catalog") as String
		this.inclusionPatterns = (properties.getOrDefault("mdd.importer.jdbc.table.filter.inclusion", "") as String).split(",").filter[!empty].map[Pattern.compile(it)].toList
		this.exclusionPatterns = (properties.getOrDefault("mdd.importer.jdbc.table.filter.exclusion", "") as String).split(",").map[Pattern.compile(it)].toList
	}

	private def Map<String, String> filterProperties(Properties properties, String prefix) {
		return new ParsedProperties(properties).filterProperties(prefix)
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
		importModel(catalog)
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
		
		jpaProperties = new Properties()
		
		jpaProperties.put("mdd.generator.jpa.preserveSchema", "true")
		jpaProperties.put("mdd.generator.jpa.preserveData", "true")
		
		val schemas = catalog.schemas.filter[
			(selectedSchemaName == null || selectedSchemaName == it.name)
			&&
			(selectedCatalogName == null || selectedCatalogName == it.catalogName)
		] 
		schemas.forEach [ schema |
			val tables = catalog.getTables(schema).filter[table | table.name.tableIncluded].toMap[name]
			
			if (!tables.empty)
				generated.put(toPackageName(schema.name) + ".tuml", generatePackage(catalog, schema, tables))
		]
		generated.put('generator-jpa.mdd.properties', generateProperties(jpaProperties, "Generated from a reverse engineered database"))
		return generated
	}
	
	def CharSequence generateProperties(Properties properties, String description) {
		val writer = new StringWriter()
		properties.store(writer, description)
		return writer.toString
	}
	
	def String toPackageName(String originalSchemaName) {
		val schemaName = (schemaRenames.get(originalSchemaName) ?: originalSchemaName)
		fromSchemaToModel(schemaName).toLowerCase
	}

	def CharSequence generatePackage(Catalog catalog, Schema schema, Map<String, Table> tables) {
		val packageName = schema.name.toPackageName
		jpaProperties.put("mdd.generator.jpa.mapping." + packageName, schema.name)
		
		val classTables = tables.values.filter[it.importedForeignKeys.empty || it.importedForeignKeys.size < it.columns.size]
		val associationTables = tables.values.filter[!it.importedForeignKeys.empty && it.importedForeignKeys.size == it.columns.size]
		'''
		/* Generated from a reverse engineered database */
			package «packageName»;
			
			import mdd_types;
			import mdd_media;
			
			«classTables.map[table | generateClass(catalog, schema, table, tables.keySet)].join()»
			«associationTables.map[table | generateAssociation(catalog, schema, table, tables.keySet)].join()»
			
			end.
		'''
	}

	def CharSequence generateClass(Catalog catalog, Schema schema, Table table, Set<String> tableNames) {
		val className = table.name.toClassName()
		val packageName = schema.name.toPackageName
		jpaProperties.put("mdd.generator.jpa.mapping." + packageName + "." + className, table.name)
		
		val foreignKeys = table.importedForeignKeys.filter[tableNames.contains(columnReferences.head.foreignKeyColumn.referencedColumn.parent.name)].toMap[columnReferences.head.foreignKeyColumn.toReferenceName]
		val foreignKeyColumns = foreignKeys.values.map[it.columnReferences.map[foreignKeyColumn]].flatten.toMap[name]
		val nonPkColumns = table.columns.filter[!partOfPrimaryKey]
		val ordinaryColumns = nonPkColumns.filter[!foreignKeyColumns.containsKey(it.name)].toMap[name]
		
		'''
            «table.withOptionalRemarks»class «className»
                «ordinaryColumns.values.map[column | generateAttribute(catalog, schema, table, column)].join()»
                «foreignKeys.values.map[fk | generateReference(catalog, schema, table, fk)].join()»
            end;
		'''
	}
    
    def withOptionalRemarks(DescribedObject described) {
        if (described.hasRemarks)
        '''
        (*
        «described.remarks»
        *)
        '''
    }

	def CharSequence generateAssociation(Catalog catalog, Schema schema, Table table, Set<String> tableNames) {
		val foreignKeys = table.importedForeignKeys

		'''
			«table.withOptionalRemarks»association «table.toClassName()»
				«foreignKeys.map[fk | generateAssociationEnd(catalog, schema, table, fk)].join()»
			end;
		'''
	}

	def CharSequence generateAssociationEnd(Catalog catalog, Schema schema, Table table, ForeignKey key) {
		val columnReference = key.columnReferences.
			head
		// «fk.columnReferences.join('\\n')»
		'''
			«columnReference.foreignKeyColumn.withOptionalRemarks»navigable role «columnReference.foreignKeyColumn.toReferenceName» : «columnReference.primaryKeyColumn.parent.toClassName»«columnReference.foreignKeyColumn.generateMultiplicity»;
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
		val schemaName = originalSchemaName.toLowerCase.replaceAll("[\\s-]", "_").removeStart("\"").removeEnd("\"")
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
	    return column.name.toAttributeName	
	}
	def CharSequence toAttributeName(String columnName) {
		val attributeName = fromSchemaToModel(columnName).toFirstLower
		if (TextUMLConstants.KEYWORDS.contains(attributeName.toLowerCase))
			return ('\\' + attributeName)
		return attributeName
	}

	def CharSequence generateAttribute(Catalog catalog, Schema schema, Table table, Column column) {
		val className = table.name.toClassName
		val packageName = schema.name.toPackageName
		val attributeName = column.name.toAttributeName
		jpaProperties.put("mdd.generator.jpa.mapping." + packageName + "." + className + "." + attributeName, column.name)
		
		val modifiers = getAttributeModifiers(column).map['''«it» '''].
			join()
		// «column.name» «column.columnDataType» («column.columnDataType.javaSqlType.javaSqlTypeGroup»)
		'''
			«column.withOptionalRemarks»«modifiers»attribute «attributeName» : «generateAttributeType(column.columnDataType)»«column.generateMultiplicity»«column.generateDefaultValue»;
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
		val referenceName = columnReference.foreignKeyColumn.toReferenceName
		val className = table.name.toClassName
		val packageName = schema.name.toPackageName
		jpaProperties.put("mdd.generator.jpa.mapping." + packageName + "." + className + "." + referenceName, columnReference.foreignKeyColumn.name)
		
		'''
			reference «referenceName» : «columnReference.primaryKeyColumn.parent.toClassName»«columnReference.foreignKeyColumn.generateMultiplicity»;
		'''
	}

	def boolean isSpecialColumn(Column toCheck) {
		return specialColumns.containsKey(toCheck.name)
	}

	def boolean isConstantColumn(Column toCheck) {
		val isConstant = constantColumns.containsKey(toCheck.name) || constantColumns.containsKey(toCheck.shortName)
		return isConstant
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
	
	def boolean isTableIncluded(String tableName) {
		if (inclusionPatterns.exists[matcher(tableName).matches])
			return true
		if (exclusionPatterns.exists[matcher(tableName).matches])
			return false
		return inclusionPatterns.empty
	}
	
}
