package com.abstratt.mdd.modelrenderer.uml2dot;

import static com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.*;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Relationship;

import com.abstratt.mdd.core.util.ElementUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.modelrenderer.IRenderingSession;
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter;

public abstract class AbstractRelationshipRenderer<T extends Relationship>
		implements IElementRenderer<T> {

    @Override
	public final boolean renderObject(T element, IndentedPrintWriter out,
			IRenderingSession context) {
		return basicRenderObject(element, out, context);
	}

	protected abstract boolean basicRenderObject(T element,
			IndentedPrintWriter out, IRenderingSession<Element> context);

	protected boolean shouldRender(IRenderingSession<Element> context,
			Element source, Element destination) {
		boolean isModelLibrary = MDDUtil.getRootPackage(destination.getNearestPackage()).isModelLibrary();
		boolean showModelLibraries = context.getSettings().getBoolean(SHOW_ELEMENTS_IN_LIBRARIES);
		if (isModelLibrary && !showModelLibraries)
			return false;

		ShowCrossPackageElementOptions crossPackageElementOption = context.getSettings().getSelection(ShowCrossPackageElementOptions.class);
		switch (crossPackageElementOption) {
		case Never:
			return EcoreUtil.equals(source.getNearestPackage(), destination
					.getNearestPackage());
		case Immediate:
			return EcoreUtil.isAncestor(context.getRoot(), source);
		case Always:
		    return true;
		case Local:
		    return ElementUtils.sameRepository(context.getRoot(), destination);
		}
		// should never run
		return false;
	}
}
