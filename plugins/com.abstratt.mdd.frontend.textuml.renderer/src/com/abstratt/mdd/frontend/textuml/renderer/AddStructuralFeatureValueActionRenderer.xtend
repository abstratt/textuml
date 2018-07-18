//package com.abstratt.mdd.frontend.textuml.renderer
//
//import org.eclipse.uml2.uml.AddStructuralFeatureValueAction
//import org.eclipse.uml2.uml.InputPin
//import com.abstratt.mdd.modelrenderer.IEObjectRenderer
//import com.abstratt.mdd.modelrenderer.IRenderingSession
//import com.abstratt.mdd.modelrenderer.IRendererSelector
//import org.eclipse.uml2.uml.Action
//
//
//import static extension com.abstratt.mdd.core.util.ActivityUtils.*
//import static extension com.abstratt.mdd.core.util.FeatureUtils.*
//
//class AddStructuralFeatureValueActionRenderer extends ActionRenderer<AddStructuralFeatureValueAction> {
//	
//	override protected renderAction(AddStructuralFeatureValueAction action, IRendererSelector<Action> selector) {
//		'''
//			«renderAnotherAction(action.object.sourceAction, selector)».«action.structuralFeature.name» := «renderAnotherAction(action.value.sourceAction, selector)»;
//		'''
//	}
//}
