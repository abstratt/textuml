//package com.abstratt.mdd.frontend.textuml.renderer
//
//import org.eclipse.uml2.uml.CallOperationAction
//import com.abstratt.mdd.modelrenderer.IRenderingSession
//import static extension com.abstratt.mdd.core.util.ActivityUtils.*
//import com.abstratt.mdd.modelrenderer.IRendererSelector
//import org.eclipse.uml2.uml.Action
//
//class CallOperationActionRenderer extends ActionRenderer<CallOperationAction> {
//	
//	override protected renderAction(CallOperationAction action, IRendererSelector<Action> selector) {
//		'''«renderAnotherAction(action.target.sourceAction, selector)»«action.operation.name»(«action.arguments.map[
//			renderAnotherAction(it.sourceAction, selector)
//		].join(', ')»)'''
//	}
//	
//}