package com.abstratt.mdd.core.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;

public class ConnectorUtils {

	/**
	 * Returns the property (part) providing the required interfaces.
	 */
	public static Property findProvidingPart(Port port) {
		return findProvidingPart(new HashSet<ConnectorEnd>(), port.getRequireds(), port);
	}

	private static Property findProvidingPart(Set<ConnectorEnd> visited, List<Interface> requireds, Port port) {
		return findProvidingPart(visited, requireds, port.getEnds());
	}

	private static Property findProvidingPart(Set<ConnectorEnd> visited, List<Interface> required,
	        ConnectorEnd connectorEnd) {
		if (!visited.add(connectorEnd))
			// already visited
			return null;
		if (connectorEnd.getRole() instanceof Port) {
			Port asPort = (Port) connectorEnd.getRole();
			if (!asPort.getProvideds().containsAll(required) && !asPort.getRequireds().containsAll(required))
				// wrong path
				return null;
			Property found = findProvidingPart(visited, required, asPort);
			if (found != null)
				return found;
			return findProvidingPart(visited, required, getAllEnds(connectorEnd));
		} else {
			Property asProperty = (Property) connectorEnd.getRole();
			return asProperty;
		}
	}

	private static Property findProvidingPart(Set<ConnectorEnd> visited, List<Interface> required,
	        List<ConnectorEnd> ends) {
		for (ConnectorEnd end : ends) {
			Property provider = findProvidingPart(visited, required, end);
			if (provider != null)
				return provider;
		}
		return null;
	}

	private static List<ConnectorEnd> getAllEnds(ConnectorEnd connectorEnd) {
		if (connectorEnd.getOwner() instanceof Connector) {
			Connector connector = (Connector) connectorEnd.getOwner();
			return connector.getEnds();
		}
		return Collections.<ConnectorEnd> emptyList();
	}

	public static BehavioredClassifier findProvidingClassifier(Port port) {
		Property findProvidingPart = findProvidingPart(port);
		return (BehavioredClassifier) (findProvidingPart == null ? null : findProvidingPart.getType());
	}
}
