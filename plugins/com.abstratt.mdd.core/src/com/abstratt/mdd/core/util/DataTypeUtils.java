package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

public class DataTypeUtils {

	public static String computeTupleTypeName(DataType dataType) {
		List<String> slotNames = new ArrayList<String>();
		List<Type> types = new ArrayList<Type>();
		for(Property p : dataType.getAllAttributes()) {
			slotNames.add(p.getName());
			types.add(p.getType());
		}
		return computeTupleTypeName(slotNames, types);
	}

	public static String computeTupleTypeName(List<String> slotNames, List<Type> slotTypes) {
		List<String> components = new ArrayList<String>();
		for (int i = 0; i < slotNames.size(); i++) {
			String componentName = StringUtils.trimToEmpty(slotNames.get(i)) + " : " + slotTypes.get(i).getQualifiedName();
			components.add(componentName);
		}
		return "[" + StringUtils.join(components, ", ") + "]";
	}


	public static DataType findOrCreateDataType(Package currentPackage, List<String> slotNames,
			List<Type> slotTypes) {
		String tupleTypeName = computeTupleTypeName(slotNames, slotTypes);
		DataType dataType = (DataType) currentPackage.getOwnedType(tupleTypeName, false, UMLPackage.Literals.DATA_TYPE, false);
		if (dataType == null) {
			dataType = (DataType) currentPackage.createOwnedType(null, UMLPackage.Literals.DATA_TYPE);
			for (int i = 0; i < slotNames.size(); i++)
				dataType.createOwnedAttribute(slotNames.get(i), slotTypes.get(i));
			dataType.setName(computeTupleTypeName(dataType));
		}
		return dataType;
	}

}
