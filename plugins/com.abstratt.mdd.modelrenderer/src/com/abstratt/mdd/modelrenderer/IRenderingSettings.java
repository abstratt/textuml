package com.abstratt.mdd.modelrenderer;

public interface IRenderingSettings {
	public IRenderingSettings NO_SETTINGS = new NoRenderingSettings();
	public interface SettingsSource {
		public String getSetting(String optionKey);	
	}
	public <T> boolean isSelected(Enum<?> option);
	public <T extends Enum<T>> T getSelection(Class<T> enumerationClass);
	public boolean getBoolean(String key);
	public String getString(String key);
}