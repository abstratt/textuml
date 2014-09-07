package com.abstratt.kirra.mdd.core;

import java.util.ArrayList;
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

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.isv.IModelWeaver;
import com.abstratt.mdd.core.util.ConnectorUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
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
		final Stereotype entityStereotype = repository.findNamedElement("kirra::Entity", Literals.STEREOTYPE, null);
		final Stereotype serviceStereotype = repository.findNamedElement("kirra::Service", Literals.STEREOTYPE, null);
		final Stereotype actionStereotype = repository.findNamedElement("kirra::Action", Literals.STEREOTYPE, null);
		final Stereotype finderStereotype = repository.findNamedElement("kirra::Finder", Literals.STEREOTYPE, null);
		final Stereotype essentialStereotype = repository.findNamedElement("kirra::Essential", Literals.STEREOTYPE, null);
		
		final Class baseObject = repository.findNamedElement("mdd_types::Object", Literals.CLASS, null);
		final Class stringType = repository.findNamedElement("mdd_types::String", Literals.CLASS, null);
		
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
		
		// collect all entities
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
				if (!asClass.getAppliedStereotypes().isEmpty() && asClass.getStereotypeApplication(userStereotype) == null)
				    return false;
				return true;
			}
		}, true);
		// apply entity stereotype
		for (Class entity : entities) {
			StereotypeUtils.safeApplyStereotype(entity, entityStereotype);
			// create username property
			if (entity.isStereotypeApplied(userStereotype) && FeatureUtils.findAttribute(entity, "username", false, true) == null) {
				Property username = entity.createOwnedAttribute("username", stringType);
				username.setIsReadOnly(true);
			}
		}
		for (Class entity : entities) {
			// apply operation stereotypes
			for (Operation operation : entity.getOperations()) {
				Type returnType = operation.getType();
				if (returnType != null && operation.isStatic() && operation.getReturnResult().isMultivalued() && returnType.isStereotypeApplied(entityStereotype))
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
			for (Property property : entity.getAttributes()) {
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
	}
}
