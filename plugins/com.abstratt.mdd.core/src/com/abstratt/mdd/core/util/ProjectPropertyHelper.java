package com.abstratt.mdd.core.util;

import java.util.Properties;

import com.abstratt.mdd.core.IRepository;

public class ProjectPropertyHelper {
    public static boolean isLibrary(Properties projectProperties) {
        return Boolean.TRUE.toString().equals(projectProperties.get(IRepository.LIBRARY_PROJECT));
    }
}
