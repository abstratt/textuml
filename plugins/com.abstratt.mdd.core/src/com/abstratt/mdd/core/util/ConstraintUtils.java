package com.abstratt.mdd.core.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;

public class ConstraintUtils {
	
	public static boolean isConstraintParameterless(Activity constraintActivity) {
		return ActivityUtils.getClosureInputParameters(constraintActivity).isEmpty();
	}
	/**
	 * Returns all constraints that apply to the given operation parameter.
	 * 
	 * @param parameter
	 * @return
	 */
	public static List<Constraint> getParameterConstraints(Parameter parameter) {
        List<Constraint> operationConstraints = parameter.getOperation().getPreconditions();
        List<Constraint> result = new LinkedList<>();
		for (Constraint constraint : operationConstraints) {
            Behavior constraintBehavior = ActivityUtils.resolveBehaviorReference(constraint.getSpecification());
            List<Parameter> constraintInputParameters = FeatureUtils.filterParameters(constraintBehavior.getOwnedParameters(),
                    ParameterDirectionKind.IN_LITERAL);
            if (constraintInputParameters.size() == 1) {
            	// we can't handle constraints on multiple parameters
            	Optional<Parameter> matchingParameter = constraintInputParameters.stream().filter(it -> 
            		constraintBehavior.getOwnedParameter(parameter.getName(), parameter.getType()) != null
    			).findAny();
            	if (matchingParameter.isPresent() && matchingParameter.get().getDirection() == ParameterDirectionKind.IN_LITERAL) {
            		result.add(constraint);
            	}
            }
		}
        return result;
	}
	public static boolean hasParameterConstraints(Parameter parameter) {
        List<Constraint> operationConstraints = parameter.getOperation().getPreconditions();
		for (Constraint constraint : operationConstraints) {
            Behavior constraintBehavior = ActivityUtils.resolveBehaviorReference(constraint.getSpecification());
            List<Parameter> constraintInputParameters = FeatureUtils.filterParameters(constraintBehavior.getOwnedParameters(),
                    ParameterDirectionKind.IN_LITERAL);
            if (constraintInputParameters.size() == 1) {
            	// we can't handle constraints on multiple parameters
            	Optional<Parameter> matchingParameter = constraintInputParameters.stream().filter(it -> 
            		constraintBehavior.getOwnedParameter(parameter.getName(), parameter.getType()) != null
    			).findAny();
            	if (matchingParameter.isPresent() && matchingParameter.get().getDirection() == ParameterDirectionKind.IN_LITERAL)
            		return true;
            }
		}
        return false;
	}
}
