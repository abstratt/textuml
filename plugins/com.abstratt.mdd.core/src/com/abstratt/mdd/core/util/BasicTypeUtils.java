package com.abstratt.mdd.core.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private static final Converter NOOP_CONVERTER = value -> value;

    static {
        converters.put("Integer", value -> Long.valueOf(value));
        converters.put("Double", value -> Double.valueOf(value));
        converters.put("Boolean", value -> Boolean.valueOf(value));
        converters.put("Date", value -> new Date(Long.valueOf(value)));
        converters.put("Time", value -> LocalTime.parse(value));
        converters.put("DateTime", value -> LocalDateTime.parse(value));
        converters.put("Blob", NOOP_CONVERTER);
        converters.put("Picture", NOOP_CONVERTER);
        converters.put("Video", NOOP_CONVERTER);
        converters.put("Sound", NOOP_CONVERTER);
        converters.put("Email", NOOP_CONVERTER);
        converters.put("Memo", NOOP_CONVERTER);
        converters.put("String", NOOP_CONVERTER);
        converters.put("Geolocation", NOOP_CONVERTER);
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
        return converters.containsKey(typeKey) && IRepository.TYPES_NAMESPACE.equals(toCheck.getPackage().getName());
    }
    
    public static boolean isBlobType(Type toCheck) {
        String typeKey = toCheck.getName();
        return converters.containsKey(typeKey) && IRepository.MEDIA_NAMESPACE.equals(toCheck.getPackage().getName());
    }

    public static Classifier findBuiltInType(String typeName) {
        Classifier builtInType = MDDCore.getInProgressRepository().findNamedElement(TypeUtils.makeTypeName(typeName),
                IRepository.PACKAGE.getType(), null);
        return builtInType;
    }
}
