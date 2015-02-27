package com.abstratt.mdd.frontend.internal.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.ObjectNode;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.ValuePin;

import com.abstratt.mdd.core.util.ActivityUtils;

class ActionInfo {
	private Action action;
	private List<ActionInfo> children = new LinkedList<ActionInfo>();
	private boolean dataFlows;
	private List<ObjectNode> inputs = new LinkedList<ObjectNode>();
	private List<ObjectNode> outputs = new LinkedList<ObjectNode>();
	private ActionInfo parent;

	public ActionInfo(ActionInfo parent, Action action, boolean dataFlows) {
		this.parent = parent;
		if (parent != null)
			parent.children.add(this);
		this.action = action;
		this.dataFlows = dataFlows;
	}

	/**
	 * Connects the output pins of the action represented by this action info 
	 * to the input pins of the action represented by destination action info.
	 *  
	 * @param destination
	 */
	private void connectTo(ActionInfo destination) {
		List<ObjectNode> allInputs = destination.inputs;
		List<ObjectNode> allOutputs = this.outputs;
		int currentOutputIndex = 0;
		for (Iterator<ObjectNode> i = allInputs.iterator(); i.hasNext() && currentOutputIndex < allOutputs.size();) {
			ObjectNode currentInput = i.next();
			if (currentInput instanceof ValuePin || !currentInput.getIncomings().isEmpty())
				// input not available, cannot connect to this one
				continue;
			final ObjectNode currentOutput = allOutputs.get(currentOutputIndex++);
			ActivityUtils.connect(((StructuredActivityNode) action.getOwner()), currentOutput, currentInput);
		}
		boolean allPinsMatched = currentOutputIndex == allOutputs.size();
        Assert.isTrue(allPinsMatched, "Not enough inputs in " + destination.action.eClass().getInstanceClassName() + " for " + this.action.eClass().getInstanceClassName() + ": inputs = " + currentOutputIndex + ", outputs = " + allOutputs.size());
	}

	public Action getAction() {
		return action;
	}

	public ActionInfo getParent() {
		return parent;
	}

	public boolean hasDataFlows() {
		return dataFlows;
	}

	/**
	 * Connects this action output pins with its parent actions input pins.
	 */
	public void makeConnections() {
		if (parent != null && parent.hasDataFlows())
			connectTo(parent);
	}

	public void registerInput(ObjectNode input) {
		this.inputs.add(input);
	}

	public void registerOutput(ObjectNode output) {
		this.outputs.add(output);
	}

	public String toString() {
		return action.toString();
	}

}
