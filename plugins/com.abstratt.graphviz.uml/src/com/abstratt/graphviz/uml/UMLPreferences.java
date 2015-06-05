package com.abstratt.graphviz.uml;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences;
import com.abstratt.modelrenderer.IRenderingSettings.SettingsSource;

public class UMLPreferences {
	
	public static class Initializer extends AbstractPreferenceInitializer {

		@Override
		public void initializeDefaultPreferences() {
			IEclipsePreferences defaults = new DefaultScope()
					.getNode(UML.PLUGIN_ID);
			defaults.putBoolean(UML2DOTPreferences.SHOW_CLASSIFIER_STEREOTYPES, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_FEATURE_STEREOTYPES, false);
			defaults.putBoolean(UML2DOTPreferences.SHOW_RELATIONSHIP_STEREOTYPES, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_END_OWNERSHIP, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_STRUCTURAL_FEATURE_VISIBILITY, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_END_MULTIPLICITY, false);
			defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_NAME, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_END_NAME, true);
			defaults.putBoolean(UML2DOTPreferences.OMIT_CONSTRAINTS_FOR_NAVIGABILITY , false);
			defaults.putBoolean(UML2DOTPreferences.SHOW_PARAMETERS, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_RETURN_PARAMETER, true);
			defaults.putBoolean(UML2DOTPreferences.SHOW_PARAMETER_NAMES, false);
			defaults.putBoolean(UML2DOTPreferences.SHOW_PARAMETER_DIRECTION, false);
			defaults.put(UML2DOTPreferences.SHOW_CLASSIFIER_COMPARTMENT, UML2DOTPreferences.ShowClassifierCompartmentOptions.Never.name());
			defaults.put(UML2DOTPreferences.SHOW_CLASSIFIER_COMPARTMENT_FOR_PACKAGE, UML2DOTPreferences.ShowClassifierCompartmentForPackageOptions.Current.name());
			defaults.put(UML2DOTPreferences.SHOW_ELEMENTS_IN_OTHER_PACKAGES, UML2DOTPreferences.ShowCrossPackageElementOptions.Immediate.name());
			defaults.putBoolean(UML2DOTPreferences.SHOW_PRIMITIVES, false);			
		}
	}
	
	public static SettingsSource getPreferences() {
		ScopedPreferenceStore prefStore = new ScopedPreferenceStore(new InstanceScope(), UML.PLUGIN_ID);
		return new PreferenceStoreSource(prefStore);
	}
}
