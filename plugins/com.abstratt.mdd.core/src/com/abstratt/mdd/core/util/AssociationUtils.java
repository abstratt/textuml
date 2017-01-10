package com.abstratt.mdd.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;

public class AssociationUtils {
    public static List<Association> allAssociations(Classifier classifier) {
        List<Association> result = new ArrayList<Association>();
        List<Classifier> allLevels = new ArrayList<Classifier>(classifier.allParents());
        allLevels.add(classifier);
        for (Classifier level : allLevels)
            result.addAll(getOwnAssociations(level));
        return result;
    }

    public static Collection<? extends Association> getOwnAssociations(Classifier level) {
		return level.getAssociations();
	}

	/**
     * Returns the member end with the given name.
     * 
     * @param level
     * @param endName
     * @return
     */
    public static Property findMemberEnd(Classifier level, String endName) {
        for (Association association : level.getAssociations())
            for (Property memberEnd : getMemberEnds(association, level))
                if (endName.equals(memberEnd.getName()))
                    return memberEnd;
        for (Classifier general : level.getGenerals()) {
            Property found = findMemberEnd(general, endName);
            if (found != null)
                return found;
        }
        return null;
    }

    /**
     * Returns the association member ends that represent the side of the given
     * participant (referring to the other participant).
     * 
     * In the case of a reflexive association, returns both ends.
     * 
     * @return a collection of member ends, with 0-2 elements (typically 1)
     */
    public static List<Property> getMemberEnds(Association association, Classifier participant) {
        List<Property> result = new ArrayList<Property>();
        for (Property memberEnd : association.getMemberEnds())
            if (memberEnd.getOtherEnd() != null && participant == memberEnd.getOtherEnd().getType())
                result.add(memberEnd);
        return result;
    }
}
