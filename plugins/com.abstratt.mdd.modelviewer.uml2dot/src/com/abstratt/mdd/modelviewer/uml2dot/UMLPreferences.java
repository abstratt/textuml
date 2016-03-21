package com.abstratt.mdd.modelviewer.uml2dot;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.abstratt.mdd.modelrenderer.IRenderingSettings.SettingsSource;
import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences;

public class UMLPreferences {

    public static class Initializer extends AbstractPreferenceInitializer {

        @Override
        public void initializeDefaultPreferences() {
            IEclipsePreferences defaults = new DefaultScope().getNode(UML.PLUGIN_ID);
            defaults.putBoolean(UML2DOTPreferences.SHOW_CLASSIFIER_STEREOTYPES, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_FEATURE_STEREOTYPES, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_RELATIONSHIP_STEREOTYPES, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_END_OWNERSHIP, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_FEATURE_VISIBILITY, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_END_MULTIPLICITY, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_NAME, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_ASSOCIATION_END_NAME, true);
            defaults.putBoolean(UML2DOTPreferences.OMIT_CONSTRAINTS_FOR_NAVIGABILITY, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_COMMENTS, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_PARAMETERS, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_RETURN_PARAMETER, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_PARAMETER_NAMES, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_PARAMETER_DIRECTION, false);
            defaults.put(UML2DOTPreferences.SHOW_CLASSIFIER_COMPARTMENT,
                    UML2DOTPreferences.ShowClassifierCompartmentOptions.NotEmpty.name());
            defaults.put(UML2DOTPreferences.SHOW_CLASSIFIER_COMPARTMENT_FOR_PACKAGE,
                    UML2DOTPreferences.ShowClassifierCompartmentForPackageOptions.Local.name());
            defaults.put(UML2DOTPreferences.SHOW_ELEMENTS_IN_OTHER_PACKAGES,
                    UML2DOTPreferences.ShowCrossPackageElementOptions.Local.name());
            defaults.putBoolean(UML2DOTPreferences.SHOW_ELEMENTS_IN_LIBRARIES, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_PRIMITIVES, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_CLASSES, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_INTERFACES, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_STATEMACHINES, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_OPERATIONS, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_ATTRIBUTES, true);
            defaults.putBoolean(UML2DOTPreferences.SHOW_DATATYPES, false);
            defaults.putBoolean(UML2DOTPreferences.SHOW_SIGNALS, false);
        }
    }

    public static SettingsSource getPreferences() {
        ScopedPreferenceStore prefStore = new ScopedPreferenceStore(new InstanceScope(), UML.PLUGIN_ID);
        return new PreferenceStoreSource(prefStore);
    }
}
