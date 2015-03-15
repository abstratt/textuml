package com.abstratt.mdd.frontend.core.spi;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.ObjectNode;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Variable;

import com.abstratt.mdd.core.IRepository;

/**
 * Activity builders are used to create behavioral models.
 */
public interface IActivityBuilder {

	/**
	 * Closes the current action. After the current action is closed, its parent
	 * action becomes the current action. Automatically connects the current
	 * action's output pins to the input pins of its parent (caller) action.
	 */
	public void closeAction();
	
	public boolean isCurrentActionTerminal();

	/**
	 * Closes the current action. After the current action is closed, its parent
	 * action becomes the current action. If <code>autoConnect</code> is
	 * <code>true</code>, connects the current action's output pins to the
	 * input pins of its parent (caller) action.
	 * 
	 * @param autoConnect
	 *            whether the pins of the action being closed should be
	 *            automatically connected to the input pins of its parent
	 *            (caller)
	 */
	public void closeAction(boolean autoConnect);

	/**
	 * Closes the current ordinary block. No action can be created before a
	 * block is created.
	 */
	public void closeBlock();

	public void closeBlock(boolean autoConnect);

	/**
	 * Closes the root block. No ordinary block can be created before the root
	 * block is created. No action can be created before a block is created.
	 */
	public void closeRootBlock();

	/**
	 * Creates an action of the given action type and attaches it to the model.
	 * The action is created under the current block.
	 * <p>
	 * Actions can be nested, forming trees.
	 * </p>
	 * 
	 * @param actionType
	 *            the action type, commonly obtained from {@link UML2Package}
	 * @return the created action
	 */
	public Action createAction(EClass actionType);

	/**
	 * Convenience operation, equivalent to:
	 * 
	 * <pre>
	 * createBlock(activityNodeClass, false);
	 * </pre>
	 * 
	 * @see #createBlock(EClass, boolean)
	 */
	public StructuredActivityNode createBlock(EClass activityNodeClass);

	/**
	 * Creates a new ordinary block of actions, which will become the current
	 * block until it is closed. Blocks are structured activity nodes.
	 * 
	 * @param activityNodeClass
	 * @param dataFlows
	 *            a boolean indicating whether data flows into or from the block
	 * 
	 * @see #closeBlock()
	 * @see #getCurrentBlock()
	 */
	public StructuredActivityNode createBlock(EClass activityNodeClass, boolean dataFlows);

	/**
	 * Creates a new root block of actions for the current activity, which will
	 * become the current block until it is closed. Blocks are structured
	 * activity nodes.
	 * 
	 * @param activity
	 * @return the activity created
	 * @throws AbortedCompilationException
	 *             if no behavioral feature exists with the given name
	 * @see #closeActivity()
	 * @see #getCurrentActivity()
	 */
	public StructuredActivityNode createRootBlock(Activity activity);

	/**
	 * Pushes the given block on top of the block stack. The given 
	 * block will become the new {@link #getCurrentBlock()}.
	 * 
	 * @param block the new current block
	 */
	public void enterBlock(ActivityNode block);

	/**
	 * Returns the current activity.
	 * 
	 * @return the current activity, or <code>null</code>
	 * @see #createActivity(Package, BehavioralFeature)
	 */
	public Activity getCurrentActivity();

	/**
	 * Returns the current block. Fails if no current block exists.
	 * 
	 * @return the current block
	 */
	public StructuredActivityNode getCurrentBlock();

	/**
	 * Returns the root action for the last statement processed. If no statement
	 * has been fully processed yet, returns <code>null</code>.
	 * 
	 * @return the root action for the last statement, or <code>null</code>
	 * @see #createAction(EClass)
	 */
	public Action getLastRootAction();

	/**
	 * Returns the repository this builder corresponds to.
	 * 
	 * @return the repository for this builder
	 */
	public IRepository getRepository();

	/**
	 * Returns a variable with the given name. If no variable with the given
	 * name is visible in the current block, returns <code>null</code>.
	 * 
	 * @param variableName
	 *            the name of the variable to look up
	 * @return a variable, or <code>null</code>
	 */
	public Variable getVariable(String variableName);

	/**
	 * Returns whether this builder is in debug mode.
	 * 
	 * @return a boolean indicating whether debug is enabled
	 */
	public boolean isDebug();

	/**
	 * Leaves the current block (pops it from the stack).
	 * 
	 * @see #enterBlock(ActivityNode)
	 */
	public void leaveBlock();

	/**
	 * Registers the given object node as input for the current action.
	 * 
	 * @param input
	 *            the input pin to register
	 * @return the given input pin
	 */
	public <T extends ObjectNode> T registerInput(T input);

	/**
	 * Registers the given object node as output for the current action.
	 * 
	 * @param output
	 *            the object node to register as output
	 * @return the given object node
	 */
	public <T extends ObjectNode> T registerOutput(T output);

	public Variable getReturnValueVariable();
}