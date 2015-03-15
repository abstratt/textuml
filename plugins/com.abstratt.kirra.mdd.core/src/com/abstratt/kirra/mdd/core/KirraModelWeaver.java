package com.abstratt.kirra.mdd.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.ModelException;
import com.abstratt.mdd.core.UnclassifiedProblem;
import com.abstratt.mdd.core.isv.IModelWeaver;
import com.abstratt.mdd.core.util.ConnectorUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.StereotypeUtils;

/**
 * A model weaver for turning plain UML models into Kirra-compatible models.
 */
public class KirraModelWeaver implements IModelWeaver {

	public void packageCreated(IRepository repository, Package created) {
		Profile kirraProfile = (Profile) repository.loadPackage(URI.createURI(KirraMDDCore.KIRRA_URI));
		Package types = repository.findPackage(IRepository.TYPES_NAMESPACE, null);
		Package extensions = repository.findPackage(IRepository.EXTENSIONS_NAMESPACE, null);
		created.applyProfile(kirraProfile);
		created.createPackageImport(types);
		created.createPackageImport(extensions);
		created.createPackageImport(kirraProfile);
	}

	/**
	 * Applies the Kirra stereotypes wherever it makes sense.
	 * Automatically creates reference-like associations for attributes whose types are entities themselves.  
	 * Does nothing for a class that is already marked as an Entity.
	 */
	@Override
	public void repositoryComplete(IRepository repository) {
		final Stereotype userStereotype = repository.findNamedElement("kirra::User", Literals.STEREOTYPE, null);
		final Stereotype debuggableStereotype = repository.findNamedElement(MDDExtensionUtils.DEBUGGABLE_STEREOTYPE, Literals.STEREOTYPE, null);		
		final Stereotype entityStereotype = repository.findNamedElement("kirra::Entity", Literals.STEREOTYPE, null);
		final Stereotype serviceStereotype = repository.findNamedElement("kirra::Service", Literals.STEREOTYPE, null);
		final Stereotype actionStereotype = repository.findNamedElement("kirra::Action", Literals.STEREOTYPE, null);
		final Stereotype finderStereotype = repository.findNamedElement("kirra::Finder", Literals.STEREOTYPE, null);
		final Stereotype essentialStereotype = repository.findNamedElement("kirra::Essential", Literals.STEREOTYPE, null);
		
		final Class baseObject = repository.findNamedElement("mdd_types::Object", Literals.CLASS, null);
		
		if (baseObject == null || userStereotype == null || entityStereotype == null || actionStereotype == null || finderStereotype == null || essentialStereotype == null)
			return;

		// collect all services
		final List<BehavioredClassifier> services = new ArrayList<BehavioredClassifier>();
		repository.findAll(new EObjectCondition() {
			@Override
			public boolean isSatisfied(EObject eObject) {
				if (UMLPackage.Literals.PORT != eObject.eClass())
					return false;
				BehavioredClassifier serviceClass = ConnectorUtils.findProvidingClassifier((Port) eObject);
				if (serviceClass != null) {
					services.add(serviceClass);
					StereotypeUtils.safeApplyStereotype(serviceClass, serviceStereotype);
				}
				return false;
			}
		}, true);
		
		// collect all entity-candidate classes
		final List<Class> entities = repository.findAll(new EObjectCondition() {
			@Override
			public boolean isSatisfied(EObject eObject) {
				if (UMLPackage.Literals.CLASS != eObject.eClass())
					return false;
				Class asClass = (Class) eObject;
				if (asClass.getName() == null)
					return false;
				if (!asClass.conformsTo(baseObject))
					return false;
				if (services.contains(asClass))
					return false;
				// we accept two stereotypes - anything else will exclude them from 
				// automatic entity stereotype application
				List<Stereotype> appliedStereotypes = new ArrayList<Stereotype>(asClass.getAppliedStereotypes());
				appliedStereotypes.removeAll(Arrays.asList(userStereotype, debuggableStereotype));
                return appliedStereotypes.isEmpty();
			}
		}, true);
		// apply entity stereotype
		for (Class entity : entities)
			StereotypeUtils.safeApplyStereotype(entity, entityStereotype);
		for (Class entity : entities) {
			// apply operation stereotypes
			for (Operation operation : entity.getOperations()) {
				Type returnType = operation.getType();
				if (returnType != null && operation.isStatic() && operation.isQuery())
					StereotypeUtils.safeApplyStereotype(operation, finderStereotype);
				else if (!operation.isQuery() && VisibilityKind.PUBLIC_LITERAL == operation.getVisibility())
					StereotypeUtils.safeApplyStereotype(operation, actionStereotype);
			}
			for (Property property : entity.getAttributes())
				if (property.getName() != null && property.getType() != null && !property.getName().startsWith("_") && property.getUpper() == 1 && property.getLower() == 1)
					// only properties that are required, single-valued and do not start with '_' are marked as essential
					StereotypeUtils.safeApplyStereotype(property, essentialStereotype);
		}
		// ensure properties that refer to entities are part of associations (just like references)
		for (Class entity : entities)
			for (Property property : entity.getAttributes())
			    if (KirraHelper.isRegularProperty(property)) {
    				Type propertyType = property.getType();
    				if (propertyType != null && propertyType.isStereotypeApplied(entityStereotype) && property.getAssociation() == null) {
    					final Association newAssociation =
    						(Association) entity.getNearestPackage().createPackagedElement(null,
    										UMLPackage.Literals.ASSOCIATION);
    					newAssociation.getMemberEnds().add(property);
    					// automatically created owned end
    					newAssociation.createOwnedEnd(null, entity);
    				}
			    }
		// ensure user entities have a username property
		for (Class entity : entities)
		    if (entity.isStereotypeApplied(userStereotype)) {
		        if (KirraHelper.getUsernameProperty(entity) == null) {
		            UnclassifiedProblem problem = new UnclassifiedProblem("No user name property in user entity: '" + entity.getQualifiedName() + "'. A user name property is declared as: 'readonly id attribute <name> : String;'");
		            problem.setAttribute(IProblem.FILE_NAME, MDDExtensionUtils.getSource(entity));
		            problem.setAttribute(IProblem.LINE_NUMBER, MDDExtensionUtils.getLineNumber(entity));
		            throw new ModelException(problem);
		        }
		    }
	}
}
