/**
 * 
 */
package com.abstratt.mdd.modelrenderer;

public final class NoRenderingSettings implements IRenderingSettings {
    @Override
    public boolean getBoolean(String key) {
        return false;
    }

    @Override
    public <T extends Enum<T>> T getSelection(Class<T> enumerationClass) {
        return enumerationClass.getEnumConstants()[0];
    }

    @Override
    public <T> boolean isSelected(Enum<?> option) {
        return option.ordinal() == 0;
    }

    @Override
    public String getString(String key) {
        return null;
    }
}