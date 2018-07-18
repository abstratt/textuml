//package com.abstratt.mdd.frontend.textuml.renderer
//
//import com.abstratt.mdd.modelrenderer.IRendererSelector
//import com.abstratt.mdd.modelrenderer.IRenderingSession
//import com.abstratt.mdd.modelrenderer.IndentedPrintWriter
//import org.eclipse.emf.ecore.EObject
//import org.eclipse.uml2.uml.Action
//import org.eclipse.uml2.uml.AddVariableValueAction
//import org.eclipse.uml2.uml.Element
//
//import static extension com.abstratt.mdd.core.util.ActivityUtils.*
//
//class AddVariableValueActionRenderer extends ActionRenderer<AddVariableValueAction>  {
//	
//	override protected renderAction(AddVariableValueAction action, IRendererSelector<Action> selector) {
//		val valueAction = action.value.sourceAction
//		val CharSequence value = renderAnotherAction(valueAction, selector)
//		val variable = action.variable
//		return if (variable.isReturnVariable)
//		'''
//		return «value»;
//		'''
//		else
//		'''
//		«variable.name» := «value»;
//		'''
//	}
//	
//}