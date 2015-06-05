package com.abstratt.mdd.modelrenderer.dot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import com.abstratt.modelrenderer.IRendererSelector;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IRenderingSettings;
import com.abstratt.modelrenderer.IndentedPrintWriter;
import com.abstratt.modelrenderer.RenderingSession;
import com.abstratt.pluginutils.LogUtils;

public class DOTRendering implements DOTRenderingConstants {
	private static final String ID = DOTRendering.class.getPackage().getName();

	
	public static byte[] generateDOTFromModel(URI modelURI,
			IRendererSelector<?, ?> selector, IRenderingSettings settings)
			throws CoreException {
        return generateDOTFromModel(modelURI, selector, settings, new HashMap<String, Map<String,Object>>());
	}
	
	public static byte[] generateDOTFromModel(URI modelURI,
			IRendererSelector<?, ?> selector, IRenderingSettings settings, Map<String, Map<String, Object>> defaultDotSettings)
			throws CoreException {
		org.eclipse.emf.common.util.URI emfURI = org.eclipse.emf.common.util.URI
				.createURI(modelURI.toASCIIString());
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(emfURI);
		try {
			// TODO cache option map and use XMLResource constants (requires
			// dependency change)
			Map<String, Object> resourceOptions = new HashMap<String, Object>();
			resourceOptions.put("DISABLE_NOTIFY", Boolean.TRUE);	
			resource.load(resourceOptions);
			Collection<EObject> contents = resource.getContents();
			StringWriter sw = new StringWriter();
			IndentedPrintWriter out = new IndentedPrintWriter(sw);
			IRenderingSession session = new RenderingSession(selector,
					settings, out);
			printPrologue(emfURI.trimFileExtension().lastSegment(), defaultDotSettings, out);
			boolean anyRendered = session.renderAll(contents);
			if (!anyRendered) {
			    out.println("NIL [ label=\"No objects selected for rendering\"]");
			}
			printEpilogue(out);
			out.close();
			byte[] dotContents = sw.getBuffer().toString().getBytes();
			if (Boolean.getBoolean(ID + ".showDOT")) {
				LogUtils.log(IStatus.INFO, ID, "DOT output for " + modelURI,
						null);
				LogUtils.log(IStatus.INFO, ID, sw.getBuffer().toString(), null);
			}
			if (Boolean.getBoolean(ID + ".showMemory"))
				System.out.println("*** free: "
						+ toMB(Runtime.getRuntime().freeMemory())
						+ " / total: "
						+ toMB(Runtime.getRuntime().totalMemory()) + " / max: "
						+ toMB(Runtime.getRuntime().maxMemory()));
			return dotContents;
		} catch (FileNotFoundException e) {
			// file was deleted before we could read it - that is alright, don't
			// make a fuss
			return null;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, ID, "", e));
		} catch (RuntimeException e) {
			// invalid file formats might cause runtime exceptions
			throw new CoreException(new Status(IStatus.ERROR, ID, "", e));
		} finally {
			try {
				unloadResources(resourceSet);
			} catch (RuntimeException re) {
				logUnexpected("Unloading resources", re);
			}
		}
	}

	private static void printEpilogue(IndentedPrintWriter w) {
		w.exitLevel();
		w.println();
		w.println("}"); //$NON-NLS-1$
	}

	private static void printPrologue(String modelName, Map<String, Map<String, Object>> defaultDotSettings, IndentedPrintWriter w) {
		w.println("graph " + modelName + " {"); //$NON-NLS-1$ //$NON-NLS-2$
		w.enterLevel();
		DOTRenderingUtils.addAttribute(w, "ranksep", "0.8");
		DOTRenderingUtils.addAttribute(w, "nodesep", "0.85");
		DOTRenderingUtils.addAttribute(w, "nojustify", "true");
		dumpDotSettings(w, defaultDotSettings.get(DOTRenderingConstants.GLOBAL_SETTINGS_KEY));
		w.println("graph [");
		w.enterLevel();
		// DOTRenderingUtils.addAttribute(w, "outputorder", "edgesfirst");
		// DOTRenderingUtils.addAttribute(w, "packmode", "graph");
		// DOTRenderingUtils.addAttribute(w, "pack", 40);
		// DOTRenderingUtils.addAttribute(w, "ratio", "auto");
		// DOTRenderingUtils.addAttribute(w, "rank", "sink");
		// DOTRenderingUtils.addAttribute(w, "overlap", "ipsep");
		dumpDotSettings(w, defaultDotSettings.get(DOTRenderingConstants.GRAPH_SETTINGS_KEY));		
		w.exitLevel();
		w.println("]");
		// TODO provide choice
		w.println("node [");
		w.enterLevel();
		DOTRenderingUtils.addAttribute(w, "fontsize", 12);
		DOTRenderingUtils.addAttribute(w, "shape", "plaintext");
		dumpDotSettings(w, defaultDotSettings.get(DOTRenderingConstants.NODE_SETTINGS_KEY));
		w.exitLevel();
		w.println("]");
		w.println("edge [");
		w.enterLevel();
		DOTRenderingUtils.addAttribute(w, "fontsize", 9);
		DOTRenderingUtils.addAttribute(w, "dir", "both");
		// DOTRenderingUtils.addAttribute(w, "splines", "polyline");
		dumpDotSettings(w, defaultDotSettings.get(DOTRenderingConstants.EDGE_SETTINGS_KEY));
		w.exitLevel();
		w.println("]");
	}

	private static void dumpDotSettings(IndentedPrintWriter w, Map<String, Object> map) {
		if (map == null)
			return;
		for (Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() instanceof Integer)
				DOTRenderingUtils.addAttribute(w, entry.getKey(), (int) (Integer) entry.getValue());
			else
				DOTRenderingUtils.addAttribute(w, entry.getKey(), entry.getValue().toString());			
		}
	}

	private static void unloadResources(ResourceSet resourceSet) {
		for (Resource current : resourceSet.getResources())
			current.unload();
	}

	public static void logUnexpected(String message, Exception e) {
		LogUtils.logError(ID, message, e);
	}

	private static String toMB(long byteCount) {
		return byteCount / (1024 * 1024) + "MB";
	}
}
