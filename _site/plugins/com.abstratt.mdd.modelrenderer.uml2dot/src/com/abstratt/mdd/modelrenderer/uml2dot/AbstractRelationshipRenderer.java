package com.abstratt.mdd.modelrenderer.uml2dot;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Relationship;

import com.abstratt.mdd.modelrenderer.uml2dot.UML2DOTPreferences.ShowCrossPackageElementOptions;
import com.abstratt.modelrenderer.IEObjectRenderer;
import com.abstratt.modelrenderer.IRenderingSession;
import com.abstratt.modelrenderer.IndentedPrintWriter;

public abstract class AbstractRelationshipRenderer<T extends Relationship>
		implements IEObjectRenderer<T> {

	public final boolean renderObject(T element, IndentedPrintWriter out,
			IRenderingSession context) {
		return basicRenderObject(element, out, context);
	}

	protected abstract boolean basicRenderObject(T element,
			IndentedPrintWriter out, IRenderingSession context);

	protected boolean shouldRender(IRenderingSession context,
			Element source, Element destination) {
		ShowCrossPackageElementOptions crossPackageElementOption = context.getSettings().getSelection(ShowCrossPackageElementOptions.class);
		switch (crossPackageElementOption) {
		case Never:
			return EcoreUtil.equals(source.getNearestPackage(), destination
					.getNearestPackage());
		case Immediate:
			return EcoreUtil.isAncestor(context.getRoot(), source);
		}
		// Always
		return true;
	}
}
