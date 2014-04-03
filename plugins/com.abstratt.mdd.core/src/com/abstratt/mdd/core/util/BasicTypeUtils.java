package com.abstratt.mdd.core.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Type;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;

public class BasicTypeUtils {
	private static interface Converter {
		public Object convert(String value);
	}
	
	private final static Map<String, Converter> converters = new HashMap<String, Converter>();
	private static final Converter NOOP_CONVERTER = new Converter() {public Object convert(String v) {return v;}};
	
	static {
        converters.put("Integer", new Converter() { public Object convert(String value) { return Long.valueOf(value); }});
        converters.put("Double", new Converter() { public Object convert(String value) { return Double.valueOf(value); }});
        converters.put("Boolean", new Converter() { public Object convert(String value) { return Boolean.valueOf(value); }});
        converters.put("Date", new Converter() { public Object convert(String value) { return new Date(Long.valueOf(value)); }});
        converters.put("Memo", NOOP_CONVERTER);
        converters.put("String", NOOP_CONVERTER);
	}
	
	public static Object buildBasicValue(Type basicType, String value) {
		return findConverter(basicType).convert(value);
	}

	private static Converter findConverter(Type basicType) {
		String typeKey = basicType.getName();
		return converters.containsKey(typeKey) ? converters.get(typeKey) : NOOP_CONVERTER;
	}

	public static boolean isBasicType(Type toCheck) {
		String typeKey = toCheck.getName();
		return converters.containsKey(typeKey);
	}
	
	public static Classifier findBuiltInType(String typeName) {
		Classifier builtInType = MDDCore.getInProgressRepository().findNamedElement(
				TypeUtils.makeTypeName(typeName), IRepository.PACKAGE.getType(), null);
		return builtInType;
	}
}
