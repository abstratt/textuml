package com.abstratt.mdd.core.util;

import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;

public class ReceptionUtils {

	public static Reception findBySignal(Classifier parent, Signal signal) {
		EList<Reception> ownedReceptions;
		if (parent instanceof Class)
			ownedReceptions = ((Class) parent).getOwnedReceptions();
		else if (parent instanceof Interface)
			ownedReceptions = ((Interface) parent).getOwnedReceptions();
		else
			return null;
		for (Reception reception : ownedReceptions)
			if (reception.getSignal() == signal)
				return reception;
		if (parent instanceof Class) {
			for (Classifier general : parent.getGenerals()) {
				Reception inherited = findBySignal(general, signal);
				if (inherited != null)
					return inherited;
			}
			if (MDDExtensionUtils.isExternal(parent)) {
				for (Interface implemented : ((Class) parent).getImplementedInterfaces()) {
					Reception inherited = findBySignal(implemented, signal);
					if (inherited != null)
						return inherited;
				}
			}
		}
		return null;
	}

	public static Reception createReception(Classifier parent, String receptionName) {
		if (parent instanceof Class)
			return ((Class) parent).createOwnedReception(receptionName, null, null);
		if (parent instanceof Interface)
			return ((Interface) parent).createOwnedReception(receptionName, null, null);
		return null;
	}
}
