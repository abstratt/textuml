package com.abstratt.mdd.frontend.internal.core;

import java.util.Collection;
import java.util.Stack;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ObjectNode;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Variable;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.util.ActivityUtils;
import com.abstratt.mdd.frontend.core.spi.IActivityBuilder;

public class ActivityBuilder implements IActivityBuilder {

	private Stack<ActivityNode> activityNodes = new Stack<ActivityNode>();
	private ActionInfo currentAction = null;
	private Activity currentActivity;
	private boolean debug = Boolean.getBoolean("mdd.activityBuilder.debug");
	private ActionInfo lastTree = null;
	private IRepository repository;

	public ActivityBuilder(IRepository repository) {
		this.repository = repository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#closeAction()
	 */
	public void closeAction() {
		closeAction(true);
	}
	
	@Override
	public boolean isCurrentActionTerminal() {
	    return currentAction.getParent() == null || currentAction.getParent().getAction().getInputs().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.spi.IPackageBuilder#closeAction(boolean)
	 */
	public void closeAction(boolean autoConnect) {
		if (debug)
			System.out.println("Closing " + currentAction.getAction().eClass().getInstanceClassName());
		if (currentAction == null)
			return;
		if (currentAction.hasDataFlows() && autoConnect) {
			currentAction.makeConnections();
			if (currentAction.getParent() == null || !currentAction.getParent().hasDataFlows())
				// if closing the root action save as the last tree built
				lastTree = currentAction;
		}
		currentAction = currentAction.getParent();
	}

	public void closeBlock() {
		closeBlock(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#closeBlock()
	 */
	public void closeBlock(boolean autoConnect) {
		closeAction(autoConnect);
		leaveBlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.spi.IPackageBuilder#closeRootBlock()
	 */
	public void closeRootBlock() {
		// wrapper
		leaveBlock();
		// main
		leaveBlock();
		// assert activityNodes.isEmpty() : activityNodes;
		// assert currentActivity == null;
	}

	@Override
	public Action createAction(EClass actionType) {
		if (debug)
			System.out.println("Creating " + actionType.getInstanceClassName());
		Action newAction = (Action) getCurrentBlock().createNode(null, actionType);
		// puts a new action info on the stack
		currentAction = new ActionInfo(currentAction, newAction, true);
		return newAction;
	}
	
	public StructuredActivityNode createBlock(EClass activityNodeClass) {
		return createBlock(activityNodeClass, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#createBlock()
	 */
	public StructuredActivityNode createBlock(EClass activityNodeClass, boolean dataFlows) {
		Assert.isLegal(IRepository.PACKAGE.getStructuredActivityNode().isSuperTypeOf(activityNodeClass));
		StructuredActivityNode newNode = (StructuredActivityNode) getCurrentBlock().createNode(null, activityNodeClass);
		enterBlock(newNode);
		// puts a new action info on the stack
		currentAction = new ActionInfo(currentAction, newNode, dataFlows);
		return newNode;
	}

	public StructuredActivityNode createRootBlock(Activity currentActivity) {
		assert this.currentActivity == null;
		assert activityNodes.isEmpty();
		this.currentActivity = currentActivity;
		// create activity's root node
		StructuredActivityNode main = ActivityUtils.createBodyNode(currentActivity);
		// isolate the root node by default - if child nodes are defined, they will be isolated instead
		main.setMustIsolate(true);
		ActivityUtils.createParameterVariables(currentActivity);
		enterBlock(main);
		
		// another block level so we can always go back and insert more blocks before the current code
		StructuredActivityNode wrapper = createBlock(UMLPackage.Literals.STRUCTURED_ACTIVITY_NODE);
		enterBlock(wrapper);
		return wrapper;
	}

	public void enterBlock(ActivityNode newCurrentBlock) {
		activityNodes.push(newCurrentBlock);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.spi.IPackageBuilder#getCurrentAction()
	 */
	public Action getCurrentAction() {
		return currentAction == null ? null : currentAction.getAction();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#getCurrentActivity()
	 */
	public Activity getCurrentActivity() {
		return currentActivity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.IPackageBuilder#getCurrentBlock()
	 */
	public StructuredActivityNode getCurrentBlock() {
		return (StructuredActivityNode) activityNodes.peek();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.spi.IPackageBuilder#getLastRootAction()
	 */
	public Action getLastRootAction() {
		return lastTree == null ? null : lastTree.getAction();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#getRepository()
	 */
	public IRepository getRepository() {
		return repository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#getVariable(java.lang.String)
	 */
	public Variable getVariable(String variableName) {
		return ActivityUtils.findVariable(getCurrentBlock(), variableName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#isDebug()
	 */
	public boolean isDebug() {
		return debug;
	}

	public void leaveBlock() {
		Assert.isNotNull(currentActivity);
		activityNodes.pop();
		if (activityNodes.isEmpty())
			currentActivity = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#registerInput(org.eclipse.uml2.uml.InputPin)
	 */
	public <T extends ObjectNode> T registerInput(T input) {
		currentAction.registerInput(input);
		return input;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.abstratt.mdd.core.frontend.IPackageBuilder#registerOutput(org.eclipse.uml2.uml.OutputPin)
	 */
	public <T extends ObjectNode> T registerOutput(T output) {
		Assert.isLegal(output != null);
		currentAction.registerOutput(output);
		return output;
	}

	@SuppressWarnings("unused")
	private void validateTypes(Collection typedElements) {
		// for (Iterator i = typedElements.iterator(); i.hasNext();) {
		// TypedElement current = (TypedElement) i.next();
		// Assert.isTrue(current.getType() != null, "Missing type info: " +
		// current);
		// }
	}
	
	@Override
	public Variable getReturnValueVariable() {
		return getVariable("");
	}
}