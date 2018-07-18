//package com.abstratt.mdd.frontend.textuml.renderer
//
//import org.eclipse.uml2.uml.Action
//import static extension com.abstratt.mdd.core.util.ActivityUtils.*
//import org.eclipse.uml2.uml.StructuredActivityNode
//import com.abstratt.mdd.core.util.ActivityUtils
//import com.abstratt.mdd.modelrenderer.IEObjectRenderer
//import com.abstratt.mdd.modelrenderer.IRendererSelector
//import com.abstratt.mdd.modelrenderer.IRenderingSession
//import com.abstratt.mdd.modelrenderer.IndentedPrintWriter
//import com.abstratt.mdd.modelrenderer.RenderingUtils
//
//class StructuredActivityNodeRenderer extends ActionRenderer<StructuredActivityNode> {
//	override protected CharSequence renderAction(StructuredActivityNode action, IRendererSelector<Action> selector) { 
//	    '''
//	    begin
//	    	«action.findStatements.map[it.renderAnotherAction(selector)].join("\n")»
//	    end
//		'''
//	}
//}
