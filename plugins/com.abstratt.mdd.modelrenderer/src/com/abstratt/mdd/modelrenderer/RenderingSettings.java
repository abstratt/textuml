/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.modelrenderer;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * A reflection-based helper class for easy checking of settings. Settings are
 * read-only, and provided by a settings source.
 */
public class RenderingSettings implements IRenderingSettings {

    private SettingsSource preferences;

    public RenderingSettings(SettingsSource preferences) {
        this.preferences = preferences;
    }

    @Override
    public <T> boolean isSelected(Enum<?> option) {
        String optionKey = getKey(option.getClass());
        String currentValue = preferences.getSetting(optionKey);
        return currentValue != null && option.name().equals(currentValue);
    }

    @Override
    public <T extends Enum<T>> T getSelection(Class<T> enumerationClass) {
        String optionKey = getKey(enumerationClass);
        String currentValue = preferences.getSetting(optionKey);
        T[] options = enumerationClass.getEnumConstants();
		return Arrays.stream(options).filter(it -> it.name().equals(currentValue)).findAny().orElseGet(() -> options[0]);
    }

    private static <T extends Enum<?>> String getKey(Class<T> enumerationClass) {
        try {
            Field keyField = null;
            keyField = enumerationClass.getField("KEY");
            return (String) keyField.get(null);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(enumerationClass.getName() + " does not declare a public KEY constant");
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(enumerationClass.getName() + " does not declare a public KEY constant");
        }
    }

    @Override
    public boolean getBoolean(String key) {
    	return getBoolean(key, false);
    }
    
    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
    	String setting = preferences.getSetting(key);
        return setting == null ? defaultValue : Boolean.parseBoolean(setting);
    }

    @Override
    public String getString(String key) {
        return preferences.getSetting(key);
    }
}