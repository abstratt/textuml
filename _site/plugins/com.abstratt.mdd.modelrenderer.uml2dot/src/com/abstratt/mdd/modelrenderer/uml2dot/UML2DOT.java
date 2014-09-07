package com.abstratt.mdd.modelrenderer.uml2dot;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.uml2.uml.UMLPackage;

import com.abstratt.mdd.modelrenderer.dot.DOTRendering;
import com.abstratt.modelrenderer.IRenderingSettings;
import com.abstratt.modelrenderer.RendererSelector;

public class UML2DOT {
	public static RendererSelector getRendererSelector() {
		return new RendererSelector(UML2DOTPreferences.class.getPackage().getName(), UMLPackage.Literals.ELEMENT) {};
	}
	
	public static byte[] generateDOTFromModel(URI modelURI, IRenderingSettings settings) throws CoreException {
		Map<String, Map<String, Object>> dotDefaults = new HashMap<String, Map<String,Object>>();
		Map<String, Object> edgeSettings = new HashMap<String, Object>();
		edgeSettings.put("labeldistance", "1.7");
		edgeSettings.put("constraint", !Boolean.valueOf(settings.getBoolean(UML2DOTPreferences.OMIT_CONSTRAINTS_FOR_NAVIGABILITY)));
		edgeSettings.put("label", "");
		edgeSettings.put("arrowhead", "");
		edgeSettings.put("headlabel", "");
		edgeSettings.put("arrowtail", "");
		edgeSettings.put("taillabel", "");
		edgeSettings.put("style", "none");
		dotDefaults.put(DOTRendering.NODE_SETTINGS_KEY, edgeSettings );
		return DOTRendering.generateDOTFromModel(modelURI, getRendererSelector(), settings, dotDefaults );
	}
}
