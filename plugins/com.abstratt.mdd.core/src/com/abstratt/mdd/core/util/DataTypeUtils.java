package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.VisibilityKind;

public class DataTypeUtils {

	private static final String ANONYMOUS_PREFIX = "anonymous!";

    public static String computeTupleTypeName(List<String> slotNames, List<Type> slotTypes) {
		List<String> components = new ArrayList<String>();
		for (int i = 0; i < slotNames.size(); i++) {
			String componentName = StringUtils.trimToEmpty(slotNames.get(i)) + " : " + slotTypes.get(i).getQualifiedName();
			components.add(componentName);
		}
		return ANONYMOUS_PREFIX + "[" + StringUtils.join(components, ", ") + "]";
	}
	public static DataType findOrCreateDataType(Package currentPackage, List<String> slotNames,
			List<Type> slotTypes) {
	    // we set a name so we can easily find a similarly shaped/named data type later
		String tupleTypeName = computeTupleTypeName(slotNames, slotTypes);
		DataType dataType = (DataType) currentPackage.getOwnedType(tupleTypeName, false, UMLPackage.Literals.DATA_TYPE, false);
		if (dataType == null) {
			dataType = (DataType) currentPackage.createOwnedType(null, UMLPackage.Literals.DATA_TYPE);
			for (int i = 0; i < slotNames.size(); i++)
				dataType.createOwnedAttribute(slotNames.get(i), slotTypes.get(i));
			dataType.setName(tupleTypeName);
			dataType.setVisibility(VisibilityKind.PRIVATE_LITERAL);
		}
		return dataType;
	}
	
	public static boolean isAnonymousDataType(Type toCheck) {
	    return toCheck instanceof DataType && toCheck.getVisibility() == VisibilityKind.PRIVATE_LITERAL && toCheck.getName().startsWith(ANONYMOUS_PREFIX);
	}

}
