package com.abstratt.kirra.mdd.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioralFeature;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Feature;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Reception;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.VisibilityKind;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.RepositoryService;
import com.abstratt.mdd.core.util.AssociationUtils;
import com.abstratt.mdd.core.util.BasicTypeUtils;
import com.abstratt.mdd.core.util.FeatureUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.NamedElementUtils;
import com.abstratt.mdd.core.util.StateMachineUtils;
import com.abstratt.mdd.core.util.StereotypeUtils;
import com.abstratt.pluginutils.NodeSorter;

public class KirraHelper {
    public static class Metadata {
        public Map<String, Map<String, Object>> values = new HashMap<String, Map<String,Object>>();
    }
    
    private static boolean inSession() {
        Assert.isTrue(RepositoryService.DEFAULT.isInSession());
        return RepositoryService.DEFAULT.getCurrentResource().hasFeature(Metadata.class);
    }
    
    public static List<Class> getPrerequisites(Class entity) {
        return addPrerequisites(entity, new ArrayList<Class>());
    }
    
    public static List<Class> addPrerequisites(Class entity, List<Class> collected) {
        if (!collected.contains(entity)) {
            collected.add(entity);
            for (Property relationship : getRelationships(entity))
                if (isPrimary(relationship) && isRequired(relationship))
                    addPrerequisites((Class) relationship.getType(), collected);
        }
        return collected;
    }

    protected static <T> T get(NamedElement target, String property, Callable<T> retriever) {
        ensureSession();
        Map<String, Object> cachedProperties = null;
        String identity = target.getQualifiedName();
        if (identity == null)
            identity = target.eClass().getName() + "@" + System.identityHashCode(target);
        cachedProperties = getCachedProperties(identity);
        if (cachedProperties == null)
            cachedProperties = initCachedProperties(identity);
        else 
            if (cachedProperties.containsKey(property)) {
                // WIN: from cache
                final T cachedValue = (T) cachedProperties.get(property);
                return cachedValue;
            }
        T retrieved;
        try {
            retrieved = retriever.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cachedProperties.put(property, retrieved);
        return retrieved;
    }

    private static Map<String, Object> initCachedProperties(String identity) {
        HashMap<String, Object> cachedProperties;
        getMetadataCache().values.put(identity, cachedProperties = new HashMap<String, Object>());
        return cachedProperties;
    }

    private static Map<String, Object> getCachedProperties(String identity) {
        return getMetadataCache().values.get(identity);
    }

    private static Metadata getMetadataCache() {
        return (Metadata) RepositoryService.DEFAULT.getCurrentResource().getFeature(Metadata.class);
    }

    private static void ensureSession() {
        Assert.isTrue(inSession(), "Session must be started");
    }
    
    public static boolean hasStereotype(final NamedElement type, final String stereotypeName) {
        return get(type, "hasStereotype_" + stereotypeName, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return StereotypeUtils.hasStereotype(type, stereotypeName);
            }
        });
    }
    
    private static void addApplications(org.eclipse.uml2.uml.Package target, Set<Package> collected) {
        if (isApplication(target)) {
            collected.add(target);
            for (Package it : target.getImportedPackages())
                if (!collected.contains(it))
                    addApplications(it, collected);
        }
    }

    public static Collection<Package> getApplicationPackages(Package... packages) {
        Set<Package> applicationPackages = new LinkedHashSet<Package>();
        for (Package it : packages)
            KirraHelper.addApplications(it, applicationPackages);
        return applicationPackages;
    }

    public static List<Property> getEntityRelationships(Classifier modelClass) {
        return getRelationships(modelClass, false);
    }
    
    public static List<Property> getRelationships(Classifier modelClass) {
        return getRelationships(modelClass, false);
    }

    public static List<Property> getRelationships(final Classifier modelClass, final boolean navigableOnly) {
        return get(modelClass, "getRelationships_" + navigableOnly, new Callable<List<Property>>() {
            @Override
            public List<Property> call() throws Exception {
                LinkedHashSet<Property> entityRelationships = new LinkedHashSet<Property>();
                for (org.eclipse.uml2.uml.Property attribute : modelClass.getAllAttributes())
                    if (isRelationship(attribute) && attribute.getVisibility() == VisibilityKind.PUBLIC_LITERAL)
                        entityRelationships.add(attribute);
                addAssociationOwnedRelationships(modelClass, navigableOnly, entityRelationships);
                return new ArrayList<Property>(entityRelationships);
            }
        });
    }

    protected static void addAssociationOwnedRelationships(Classifier modelClass, boolean navigableOnly, LinkedHashSet<Property> entityRelationships) {
        List<Classifier> allLevels = new ArrayList<Classifier>(modelClass.allParents());
        allLevels.add(modelClass);
        for (Classifier level : allLevels)
            for (Association association : level.getAssociations()) 
                for (Property forwardReference : AssociationUtils.getMemberEnds(association, level))
                    if (forwardReference != null && isRelationship(forwardReference, navigableOnly))
                        entityRelationships.add(forwardReference);
    }

    /**
     * Tests whether a type is an entity type. A type is an entity type if: - it
     * is a classifier - it is marked with the Entity stereotype - it has at
     * least a property, even if derived - OR ELSE USERS CAN'T MAKE SENSE OF
     * IT!!!
     */
    public static boolean isEntity(final Type type) {
        return get(type, "isEntity", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isBasicallyAnEntity(type) && !isService(type) && hasProperties((Classifier) type);
            }
        });
    }
    
    public static boolean isBasicallyAnEntity(final Type type) {
        return get(type, "isBasicallyAnEntity", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return type != null && type.getName() != null && type instanceof org.eclipse.uml2.uml.Class && (hasStereotype(type, "Entity"));
            }
        });
    }

    // RESIST THE TEMPTATION OF DELETING THIS, WE MAY CHOOSE TO USE IT AGAIN
    private static boolean hasProperties(Classifier type) {
        for (Property attribute : type.getAttributes())
            if (isProperty(attribute))
                return true;
        for (Classifier general : type.getGenerals())
            if (hasProperties(general))
                return true;
        return false;
    }

    public static boolean isUser(final Classifier classifier) {
        return get(classifier, "isUser", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !classifier.isAbstract() && hasStereotype(classifier, "User") && getUsernameProperty(classifier) != null;
            }
        });
    }
    
    public static Property getUsernameProperty(final Classifier userClass) {
        return get(userClass, "getUsernameProperty", new Callable<Property>() {
            @Override
            public Property call() throws Exception {
                for (Property property : getProperties(userClass))
                    if (isUnique(property) && !isEditable(property) && property.getType()!= null && "String".equals(property.getType().getName()))
                        return property;
                return null;
            }
        });
    }
    
    public static boolean isRequired(Property property) {
        return isRequired(property, false);
    }
    
    /**
     * 
     * @param property
     * @param creation 
     * @return
     */
    public static boolean isRequired(final Property property, final boolean creation) {
        return get(property, "isRequiredProperty_" + creation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !isReadOnly(property, creation) && isBasicallyRequired(property);
            }
        });
    }

    private static boolean isBasicallyRequired(final Property property) {
        return get(property, "isBasicallyRequiredProperty", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !(property.getType() instanceof StateMachine) && property.getLower() > 0;
            }
        });

    }

    public static boolean isRequired(final Parameter parameter) {
        return get(parameter, "isRequiredParameter", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return parameter.getLower() > 0 && parameter.getDefaultValue() == null;
            }
        });
    }

    /**
     * @param parameter
     * @param creation only for uniformity so clients call call isRequired(TypedElement, boolean) 
     * @return
     */
    public static boolean isRequired(Parameter parameter, boolean creation) {
        return isRequired(parameter);
    }

    public static boolean isAction(Operation operation) {
        return isPublic(operation) && hasStereotype(operation, "Action");
    }
    
    public static boolean isEntityOperation(Operation operation) {
        return isAction(operation) || isFinder(operation);
    }

    public static List<Parameter> getParameters(BehavioralFeature operation) {
        return FeatureUtils.filterParameters(operation.getOwnedParameters(), ParameterDirectionKind.IN_LITERAL);
    }

    public static boolean isFinder(Operation operation) {
        return isPublic(operation) && hasStereotype(operation, "Finder");
    }

    public static boolean isNamespace(org.eclipse.uml2.uml.Package package_) {
        return !(package_ instanceof Profile);
    }

    public static boolean isProperty(final Property attribute) {
        return get(attribute, "isProperty", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // no support for static properties
                return isRegularProperty(attribute) && isInstance(attribute) && attribute.getName() != null && !isBasicallyAnEntity(attribute.getType());
            }
        });
    }

    /** Avoids things such as stereotypes */
    public static boolean isRegularProperty(Property attribute) {
        return attribute.eClass() == UMLPackage.Literals.PROPERTY && attribute.getType() != null && (attribute.getType().eClass() == UMLPackage.Literals.CLASS || attribute.getType().eClass() == UMLPackage.Literals.STATE_MACHINE || attribute.getType().eClass() == UMLPackage.Literals.ENUMERATION);
    }

    public static boolean isInstance(Property attribute) {
        return !isStatic(attribute);
    }

    public static boolean isPublic(final NamedElement feature) {
        return get(feature, "isPublic", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return feature.getVisibility() == VisibilityKind.PUBLIC_LITERAL;
            }
        });

    }

    public static boolean isParentRelationship(final Property attribute) {
        return get(attribute, "isParentRelationship", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return !attribute.isMultivalued() && attribute.getOtherEnd() != null && isChildRelationship(attribute.getOtherEnd());
            }
        });
    }

    public static boolean isChildRelationship(final Property attribute) {
        return get(attribute, "isChildRelationship", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isRelationship(attribute) && attribute.getAggregation() != AggregationKind.NONE_LITERAL;
            }
        });
    }

    public static boolean isLinkRelationship(final Property attribute) {
        return get(attribute, "isLinkRelationship", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isRelationship(attribute) && attribute.getAggregation() == AggregationKind.NONE_LITERAL;
            }
        });
    }
    
    public static boolean isLikeLinkRelationship(final Property attribute) {
        return get(attribute, "isLikeLinkRelationship", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isLinkRelationship(attribute) ||
                        (isChildRelationship(attribute) && isTopLevel((Classifier) attribute.getType())) || 
                        (isParentRelationship(attribute) && isTopLevel(attribute.getClass_()));
            }
        });
    }


    public static boolean isRelationship(Property attribute) {
        return isRelationship(attribute, false);
    }

    public static boolean isRelationship(final Property attribute, boolean navigableOnly) {
        return get(attribute, "isRelationshipAttribute_"+navigableOnly, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // derived relationships might not actually have an association
                return isRegularProperty(attribute) && attribute.getName() != null
                        && isBasicallyAnEntity(attribute.getType())
                        && (attribute.isDerived() || attribute.getAssociation() != null);
            }
        });
    }

    public static String metaClass(Element element) {
        return element.eClass().getName();
    }

    public static boolean isEssential(org.eclipse.uml2.uml.Property umlAttribute) {
        return isPublic(umlAttribute) && hasStereotype(umlAttribute, "kirra::Essential");
    }

    public static boolean isDerived(final org.eclipse.uml2.uml.Property umlAttribute) {
        return get(umlAttribute, "isDerived", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return umlAttribute.isDerived() && umlAttribute.getDefaultValue() != null;
            }
        });
    }
    
    public static boolean isDerivedRelationship(final org.eclipse.uml2.uml.Property umlAttribute) {
        return isRelationship(umlAttribute) && isDerived(umlAttribute);
    }
    

    /**
     * A read-only property is not ever editable. 
     */
    public static boolean isReadOnly(org.eclipse.uml2.uml.Property umlAttribute) {
        return isReadOnly(umlAttribute, false);
    }
    
    /**
     * 
     * @param umlAttribute
     * @param creationTime is this for creation time or in general? A property may be read-only after the object is created but writable at creation time (because it is a required property).
     * @return
     */
    public static boolean isReadOnly(final org.eclipse.uml2.uml.Property umlAttribute, final boolean creationTime) {
        return get(umlAttribute, "isReadOnlyProperty_" + creationTime, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (umlAttribute.isDerived())
                    return true;
                if (umlAttribute.getType() instanceof StateMachine)
                    return true;
                if (umlAttribute.getOtherEnd() != null && umlAttribute.getOtherEnd().getAggregation() != AggregationKind.NONE_LITERAL)
                    return true;
                return umlAttribute.isReadOnly() && (!creationTime || !isBasicallyRequired(umlAttribute));
            }
        });
    }
    
    /**
     * A initializable property can be set at creation time only.
     */
    public static boolean isInitializable(org.eclipse.uml2.uml.Property umlAttribute) {
        return !isReadOnly(umlAttribute, true);
    }
    
    /**
     * An editable property can be updated any time (creation or later).
     */
    public static boolean isEditable(org.eclipse.uml2.uml.Property umlAttribute) {
        return !isReadOnly(umlAttribute, false);
    }

    public static boolean isReadOnly(final Class umlClass) {
        return get(umlClass, "isReadOnlyClass", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                List<Property> all = getPropertiesAndRelationships(umlClass);
                for (Property property : all)
                    if (!isReadOnly(property))
                        return false;
                return true;
            }
        });
    }

    public static boolean isDerived(Parameter parameter) {
        return false;
    }

    public static boolean isReadOnly(Parameter parameter) {
        return parameter.getDirection() == ParameterDirectionKind.OUT_LITERAL || parameter.getDirection() == ParameterDirectionKind.RETURN_LITERAL;
    }
    /**
     * This version is here for uniformity so clients can just invoke isReadOnly(TypedElement,Boolean) 
     */
    public static boolean isReadOnly(Parameter parameter, boolean creation) {
        return isReadOnly(parameter);
    }

    public static List<Property> getProperties(final Classifier umlClass) {
        return get(umlClass, "getProperties", new Callable<List<Property>>() {
            @Override
            public List<Property> call() throws Exception {
                List<Property> entityProperties = new ArrayList<Property>();
                addEntityProperties(umlClass, entityProperties);
                return entityProperties;
            }
        });
    }
    
    public static List<Property> getTupleProperties(final Classifier dataType) {
        return get(dataType, "getTupleProperties", new Callable<List<Property>>() {
            @Override
            public List<Property> call() throws Exception {
                List<Property> tupleProperties = new ArrayList<Property>();
                addTupleProperties(dataType, tupleProperties);
                return tupleProperties;
            }
        });
    }
    
    private static void addTupleProperties(Classifier dataType, List<Property> tupleProperties) {
        for (Classifier general : dataType.getGenerals())
            addTupleProperties(general, tupleProperties);
        for (Property attribute : dataType.getAttributes())
            if (attribute.getName() != null && isPublic(attribute) && attribute.eClass() == UMLPackage.Literals.PROPERTY)
                tupleProperties.add(attribute);
    }

    public static List<Property> getPropertiesAndRelationships(final Classifier umlClass) {
        return get(umlClass, "getPropertiesAndRelationships", new Callable<List<Property>>() {
            @Override
            public List<Property> call() throws Exception {
                LinkedHashSet<Property> entityProperties = new LinkedHashSet<Property>();
                addEntityPropertiesAndRelationships(umlClass, entityProperties);
                addAssociationOwnedRelationships(umlClass, true, entityProperties);
                return new ArrayList<Property>(entityProperties);
            }
        });
    }
    
    private static void addEntityPropertiesAndRelationships(Classifier umlClass, LinkedHashSet<Property> entityProperties) {
        for (Classifier general : umlClass.getGenerals())
            addEntityPropertiesAndRelationships(general, entityProperties);
        for (Property attribute : umlClass.getAttributes())
            if (isProperty(attribute) || isRelationship(attribute))
                entityProperties.add(attribute);
    }

    /**
     * Collects entity properties, inherited ones first.
     * 
     * @param umlClass
     * @param entityProperties
     */
    private static void addEntityProperties(Classifier umlClass, List<Property> entityProperties) {
        for (Classifier general : umlClass.getGenerals())
            addEntityProperties(general, entityProperties);
        for (Property attribute : umlClass.getAttributes())
            if (isProperty(attribute))
                entityProperties.add(attribute);
    }

    public static List<Operation> getQueries(final Class umlClass) {
        return get(umlClass, "getQueries", new Callable<List<Operation>>() {
            @Override
            public List<Operation> call() throws Exception {
                List<Operation> queries = new ArrayList<Operation>();
                for (Operation operation : umlClass.getAllOperations())
                    if (isFinder(operation))
                        queries.add(operation);
                return queries;
            }
        });
    }

    public static List<Operation> getActions(final Class umlClass) {
        return get(umlClass, "getActions", new Callable<List<Operation>>() {
            @Override
            public List<Operation> call() throws Exception {
                List<Operation> actions = new ArrayList<Operation>();
                for (Operation operation : umlClass.getAllOperations())
                    if (isAction(operation))
                        actions.add(operation);
                return actions;
            }
        });
    }
    
    public static List<Operation> getInstanceActions(final Class umlClass) {
        return get(umlClass, "getInstanceActions", new Callable<List<Operation>>() {
            @Override
            public List<Operation> call() throws Exception {
                List<Operation> instanceActions = new ArrayList<Operation>();
                for (Operation operation : getActions(umlClass))
                    if (!operation.isStatic())
                        instanceActions.add(operation);
                return instanceActions;
            }
        });
    }
    
    public static List<Operation> getEntityActions(final Class umlClass) {
        return get(umlClass, "getEntityActions", new Callable<List<Operation>>() {
            @Override
            public List<Operation> call() throws Exception {
                List<Operation> entityActions = new ArrayList<Operation>();
                for (Operation operation : getActions(umlClass))
                    if (operation.isStatic())
                        entityActions.add(operation);
                return entityActions;
            }
        });
    }

    public static boolean isConcrete(Classifier umlClass) {
        return !umlClass.isAbstract();
    }

    public static boolean isTopLevel(final Classifier umlClass) {
        return get(umlClass, "isTopLevel", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                for (Operation operation : umlClass.getAllOperations())
                    if (isFinder(operation))
                        return true;
                int parentCount = 0;
                int otherRefs = 0;
                for (Property attribute : getRelationships(umlClass))
                    if (attribute.getOtherEnd() != null)
                        if (attribute.getOtherEnd().isComposite())
                            parentCount++;
                        else if (attribute.getOtherEnd().isNavigable())
                            otherRefs++;
                // if has exactly one parent and no incoming references, it is not top-level
                return parentCount != 1 || otherRefs > 0;
            }
        });
    }

    public static boolean isStandalone(Class umlClass) {
        return true;
    }

    public static Property getMnemonic(final Classifier clazz) {
        return get(clazz, "getMnemonic", new Callable<Property>() {
            @Override
            public Property call() throws Exception {
                List<Property> properties = getProperties(clazz);
                return properties.isEmpty() ? null : properties.get(0);
            }
        });
    }
    
    public static String getDescription(Element element) {
        return MDDUtil.getDescription(element);
    }

    /**
     * The primary end in a relationship. 
     * If only one end is navigable, that end is the primary one.
     * If both ends are navigable, the non-multiple end is the primary one.
     * If both ends are multiple or non-multiple, the required end is the primary one.
     * If both ends are required or non-required, the first end is the primary one.
     * 
     *  We expect associations to have at least one navigable end, 
     *  so a relationship *always* has a primary end, and only one.
     */
    public static boolean isPrimary(final org.eclipse.uml2.uml.Property thisEnd) {
        return get(thisEnd, "isPrimary", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (thisEnd.getAssociation() == null)
                    return false;
                Property firstEnd = thisEnd.getAssociation().getMemberEnds().get(0);
                if (!thisEnd.isNavigable())
                    return false;
                Property otherEnd = thisEnd.getOtherEnd();
                if (otherEnd == null)
                    return true;
                if (!otherEnd.isNavigable())
                    return true;
                if (thisEnd.isMultivalued() != otherEnd.isMultivalued())
                    return !thisEnd.isMultivalued();
                if (isBasicallyRequired(thisEnd) != isBasicallyRequired(otherEnd))
                    return isBasicallyRequired(thisEnd);
                return firstEnd == thisEnd;
            }
        });        
    }

    public static String getSymbol(org.eclipse.uml2.uml.NamedElement sourceElement) {
        Assert.isNotNull(sourceElement.getName());
        String mangled = sourceElement.getName().replaceAll("[\\W]", "_");
        return mangled;
    }
    
    public static String getLabel(org.eclipse.uml2.uml.NamedElement sourceElement) {
        String symbol = sourceElement.getName();
        return getLabelFromSymbol(symbol);
    }

    public static String getLabelFromSymbol(String symbol) {
        return StringUtils.capitalize(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(symbol), ' ')).replaceAll("[\\w\\d]^|_", " ").replaceAll("[\\s]+", " ");
    } 
    
    public static String getName(org.eclipse.uml2.uml.NamedElement sourceElement) {
        return sourceElement.getName();
    }

    public static boolean isServiceOperation(
            final BehavioralFeature operation) {
        return get(operation, "isPrimary", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (!(operation.getOwner() instanceof Interface))
                    return false;
                if (operation instanceof Operation)
                    return ((Operation) operation).getReturnResult() != null;
                
                return operation instanceof Reception && operation.getName() != null && operation.getOwnedParameters().size() == 1 && operation.getOwnedParameters().get(0).getType() instanceof Signal;
            }
        });        
    }

    public static boolean isTupleType(final Type classifier) {
        return get(classifier, "isTupleType", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // excludes other DataTypes, such as primitives
                return classifier.getName() != null && (classifier.eClass() == Literals.DATA_TYPE || classifier.eClass() == Literals.SIGNAL) && classifier.getVisibility() != VisibilityKind.PRIVATE_LITERAL;
            }
        });
    }

    public static boolean isService(final Type umlClass) {
        return get(umlClass, "isService", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return umlClass instanceof BehavioredClassifier && hasStereotype(umlClass, "Service");
            }
        });
    }

    public static boolean isApplication(final org.eclipse.uml2.uml.Package current) {
        return get(current, "isApplication", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isKirraPackage(current) && hasKirraType(current);
            }
        });
    }
    
    public static boolean isKirraPackage(final org.eclipse.uml2.uml.Package current) {
        return get(current, "isKirraPackage", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return StereotypeUtils.hasProfile(current, "kirra");
            }
        });
    }
    
    public static boolean hasKirraType(final org.eclipse.uml2.uml.Package current) {
        for (Type type : current.getOwnedTypes())
            if (isKirraType(type))
                return true;
        return false;
    }

    private static boolean isKirraType(Type type) {
        return isEntity(type) || isService(type) || isTupleType(type);
    }

    public static boolean isUnique(final org.eclipse.uml2.uml.Property umlAttribute) {
        return get(umlAttribute, "isUnique", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return umlAttribute.isID() && umlAttribute.getLower() > 0 && !umlAttribute.isMultivalued();
            }
        });
    }

    public static void addNonInstanceActions(Class target, Collection<Operation> collected) {
        for (Operation operation : target.getAllOperations())
            if (isAction(operation) && isStatic(operation))
                collected.add(operation);
    }
    
    public static boolean isStatic(final Feature operation) {
        return get(operation, "isStaticFeature", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return operation.isStatic();
            }
        });
    }

    public static List<Operation> getNonInstanceActions(Class umlClass) {
        List<Operation> actions = new LinkedList<Operation>();
        KirraHelper.addNonInstanceActions(umlClass, actions);
        return actions;
    }

    public static boolean isInstantiable(final Class umlClass) {
        return get(umlClass, "isInstantiable", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                List<Property> all = getPropertiesAndRelationships(umlClass);
                for (Property property : all)
                    if (!isDerived(property) && !isInitializable(property) && isBasicallyRequired(property))
                        return false;
                return true;
            }
        });
    }
    
    public static boolean isUserVisible(Property property) {
        if (isRelationship(property) && !property.isNavigable())
            return false;
        if (isParentRelationship(property) && !isTopLevel((Classifier) property.getOwner()))
            return false;
        return isPublic(property);
    }
    
    public static String getApplicationName(IRepository repository, Collection<org.eclipse.uml2.uml.Package> namespaces) {
        Properties repositoryProperties = repository.getProperties();
        String applicationName = repositoryProperties.getProperty(IRepository.APPLICATION_NAME);
        if (applicationName == null)
            for (Package package_ : namespaces)
                if (isApplication(package_))
                    return getLabel(package_);
        return applicationName == null ? "App" : applicationName;
    }
    
    public static List<Class> getEntities(Collection<Package> applicationPackages) {
        List<Class> result = new ArrayList<Class>();
        for (Package current : applicationPackages)
            for (Type type : current.getOwnedTypes())
                if (KirraHelper.isEntity(type))
                    result.add((Class) type);
        return result;
    }
    
    public static List<Class> getServices(Collection<Package> applicationPackages) {
        List<Class> result = new ArrayList<Class>();
        for (Package current : applicationPackages)
            for (Type type : current.getOwnedTypes())
                if (KirraHelper.isService(type))
                    result.add((Class) type);
        return result;
    }
    
    public static List<Classifier> getTupleTypes(Collection<Package> applicationPackages) {
        List<Classifier> result = new ArrayList<Classifier>();
        for (Package current : applicationPackages)
            for (Type type : current.getOwnedTypes())
                if (KirraHelper.isTupleType(type))
                    result.add((Classifier) type);
        return result;
    }

    public static boolean isEnumeration(final Type umlType) {
        return get(umlType, "isEnumeration", new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return umlType instanceof Enumeration || umlType instanceof StateMachine;
            }
        });
    }
    
    public static List<String> getEnumerationLiterals(final Type enumOrStateMachine) {
        return get(enumOrStateMachine, "getEnumerationLiterals", new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                if (enumOrStateMachine instanceof Enumeration)
                    return NamedElementUtils.getNames(((Enumeration) enumOrStateMachine).getOwnedLiterals());
                if (enumOrStateMachine instanceof StateMachine)
                    return NamedElementUtils.getNames(StateMachineUtils.getVertices(((StateMachine) enumOrStateMachine)));
                return Arrays.<String>asList();
            }
        });
    }
    
    public static List<Class> topologicalSort(List<Class> toSort) {
        toSort = new ArrayList<Class>(toSort);
        Collections.sort(toSort, new Comparator<Class>() {
            @Override
            public int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        final Map<String, Class> nameToEntity = new HashMap<String, Class>();
        List<String> sortedRefs = new ArrayList<String>();
        for (Class entity : toSort) {
            sortedRefs.add(entity.getQualifiedName());
            nameToEntity.put(entity.getQualifiedName(), entity);
        }
        NodeSorter.NodeHandler<String> walker = new NodeSorter.NodeHandler<String>() {
            @Override
            public Collection<String> next(String vertex) {
                Collection<String> result = new HashSet<String>();
                Class entity = nameToEntity.get(vertex);
                for (Property rel : getRelationships(entity))
                    if (!isDerived(rel) && isPrimary(rel))
                        result.add(rel.getType().getQualifiedName());
                return result;
            }
        };
        try {
            sortedRefs = NodeSorter.sort(sortedRefs, walker);
        } catch (IllegalArgumentException e) {
            // too bad
        }
        toSort.clear();
        for (String typeRef : sortedRefs)
            toSort.add(0, nameToEntity.get(typeRef));
        return toSort;
    }

    public static boolean isPrimitive(Type umlType) {
        return BasicTypeUtils.isBasicType(umlType);
    }
}