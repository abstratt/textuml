/*******************************************************************************
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.query.conditions.eobjects.EObjectCondition;
import org.eclipse.emf.query.statements.FROM;
import org.eclipse.emf.query.statements.IQueryResult;
import org.eclipse.emf.query.statements.SELECT;
import org.eclipse.emf.query.statements.WHERE;
import org.eclipse.uml2.common.util.UML2Util.EObjectMatcher;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.LiteralSpecification;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.MultiplicityElement;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.StructuredActivityNode;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.MDDCore;
import com.abstratt.mdd.core.util.MDDUtil.IActivityNodeTreeVisitor.Outcome;
import com.abstratt.pluginutils.LogUtils;

/**
 * Another utility class
 */
public class MDDUtil {

	/**
	 * Source for an annotation placed on every resource generated by this tool.
	 */
	public static final String GENERATED = "com.abstratt/mdd/";
	public static final String UNIT = "com.abstratt/mdd/unit";

	public static class EClassMatcher implements EObjectMatcher {
		private EClass eClass;

		public EClassMatcher(EClass eClass) {
			super();
			this.eClass = eClass;
		}

		public boolean matches(EObject eObject) {
			return eClass.isInstance(eObject);
		}

	}

	public static interface IActivityNodeTreeVisitor {
		enum Outcome {
			CONTINUE, SKIP, STOP
		};
	}
	
	private static SAXParserFactory cachedParserFactory;
	
	/**
	 * Enhances the given qualified name with the given segment.
	 */
	public static String appendSegment(String qualifiedName, String segment) {
		Assert.isNotNull(qualifiedName);
		return qualifiedName + NamedElement.SEPARATOR + segment;
	}

	public static LiteralUnlimitedNatural createLiteralUnlimitedNatural(Package parent, Integer value) {
		LiteralUnlimitedNatural valueSpec =
						(LiteralUnlimitedNatural) parent.createPackagedElement(null, IRepository.PACKAGE
										.getLiteralUnlimitedNatural());
		valueSpec.setValue(value == null ? LiteralUnlimitedNatural.UNLIMITED : value.intValue());
		valueSpec.setType(BasicTypeUtils.findBuiltInType("Integer"));
		return valueSpec;
	}

	public static LiteralBoolean createLiteralBoolean(Package parent, Boolean value) {
		LiteralBoolean valueSpec =
						(LiteralBoolean) parent.createPackagedElement(null, IRepository.PACKAGE.getLiteralBoolean());
		valueSpec.setValue(value);
		valueSpec.setType(BasicTypeUtils.findBuiltInType("Boolean"));
		return valueSpec;
	}

	public static LiteralInteger createLiteralInteger(Package parent, Integer value) {
		LiteralInteger valueSpec =
						(LiteralInteger) parent.createPackagedElement(null, IRepository.PACKAGE.getLiteralInteger());
		valueSpec.setValue(value);
		valueSpec.setType(BasicTypeUtils.findBuiltInType("Integer"));
		return valueSpec;
	}
	
	public static LiteralString createLiteralString(Package parent, String value) {
		LiteralString valueSpec =
						(LiteralString) parent.createPackagedElement(null, IRepository.PACKAGE.getLiteralString());
		valueSpec.setValue(value);
		valueSpec.setType(BasicTypeUtils.findBuiltInType("String"));
		return valueSpec;
	}
	
	public static LiteralNull createLiteralNull(Package parent) {
		LiteralNull valueSpec = (LiteralNull) parent.createPackagedElement(null, IRepository.PACKAGE.getLiteralNull());
		return valueSpec;
	}
	
	public static LiteralSpecification createLiteralValue(Object value,
			EClass eClass, Package parentProduct) {
		if (Literals.LITERAL_BOOLEAN == eClass)
			return createLiteralBoolean(parentProduct, (Boolean) value);
		if (Literals.LITERAL_INTEGER == eClass)
			return createLiteralInteger(parentProduct, (Integer) value);
		if (Literals.LITERAL_STRING == eClass)
			return createLiteralString(parentProduct, (String) value);
		if (Literals.LITERAL_UNLIMITED_NATURAL == eClass)
			return createLiteralUnlimitedNatural(parentProduct, (Integer) value);
		if (Literals.LITERAL_NULL == eClass)
			return createLiteralNull(parentProduct);
		Assert.isLegal(false, "unsupported literal specification: " + eClass);
		// never gets here
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> filterByClass(List elements, EClass elementClass) {
		List<T> filtered = new ArrayList<T>(elements.size());
		for (Object object : elements)
			if (elementClass.isInstance(object))
				filtered.add((T) object);
		return filtered;
	}
	
	public static <T extends Element> T findSingleByClass(List<? extends Element> elements, EClass elementClass, boolean required) {
		List<T> found = filterByClass(elements, elementClass);
		if (found.size() > 2)
			throw new IllegalArgumentException("Found: " + found.size());
		if (found.isEmpty())
			if (required)
				throw new IllegalArgumentException("Found none");
			else
				return null;
		return found.get(0);
	}

	public static java.net.URI fromEMFToJava(URI emfURI) {
		return java.net.URI.create(emfURI.toString());
	}

	public static URI fromJavaToEMF(java.net.URI javaURI) {
		return URI.createURI(javaURI.toString());
	}

	public static String getArgumentListString(List<? extends TypedElement> argumentList) {
		StringBuffer name = new StringBuffer("(");
		for (TypedElement argument : argumentList) {
			name.append(getTypeName(argument.getType()));
			addMultiplicity(name, argument);
			name.append(", ");
		}
		if (!argumentList.isEmpty())
			name.delete(name.length() - ", ".length(), name.length());
		name.append(")");
		return name.toString();
	}

	protected static void addMultiplicity(StringBuffer name, TypedElement argument) {
		if (argument instanceof MultiplicityElement) {
			MultiplicityElement multiple = (MultiplicityElement) argument;
			if (multiple.isMultivalued()) {
				name.append("[" + multiple.getLower() + ", ");
				name.append(multiple.getUpper() == LiteralUnlimitedNatural.UNLIMITED ? "*" : multiple.getUpper());
				name.append("]");
			}
		}
	}

	/**
	 * Returns the qualified name for the parent and simple name given.
	 */
	public static String getChildQualifiedName(NamedElement parent, String childSimpleName) {
		return parent.getQualifiedName() + NamedElement.SEPARATOR + childSimpleName;
	}

	public static String getDisplayName(TypedElement typed) {
		final String baseTypeName = MDDUtil.getTypeName(typed.getType());
		if (!(typed instanceof MultiplicityElement))
			return baseTypeName;
		MultiplicityElement asMultiple = (MultiplicityElement) typed;
		if (!asMultiple.isMultivalued())
			return baseTypeName;
		return baseTypeName + '[' + ']';
	}

	/** Returns the first segment in the given QName. */
	public static String getFirstSegment(String qualifiedName) {
		int firstSeparator = qualifiedName.indexOf(NamedElement.SEPARATOR);
		return firstSeparator == -1 ? qualifiedName : qualifiedName.substring(0, firstSeparator);
	}

	/**
	 * Returns the last segment in the given QName.
	 */
	public static String getLastSegment(String qualifiedName) {
		int lastSeparator = qualifiedName.lastIndexOf(NamedElement.SEPARATOR);
		return lastSeparator == -1 ? qualifiedName : qualifiedName.substring(lastSeparator
						+ NamedElement.SEPARATOR.length());
	}

	/**
	 * Returns the nearest parent of the given class. Fails if cannot find one.
	 * Returns the reference itself if is an instance of the given class.
	 * 
	 * @param reference
	 *            the reference element
	 * @param eClass
	 *            the type
	 * @return any enclosing element of the given class
	 */
	public static <T extends Element> T getNearest(Element reference, EClass eClass) {
		if (eClass.isInstance(reference))
			return (T) reference;
		Assert.isTrue(reference.getOwner() != null, "No '" + eClass.getName() + "' around ");
		return getNearest(reference.getOwner(), eClass);
	}
	
	/**
	 * Returns the outermost parent of the given class. Fails if cannot find one.
	 * Returns the reference itself if is an instance of the given class and no parent can be found.
	 * 
	 * @param reference
	 *            the reference element
	 * @param eClass
	 *            the type
	 * @return the outermost enclosing element of the given class
	 */
	public static <T extends Element> T getFarthest(Element reference, EClass eClass) {
		T parent = getFarthest(reference.getOwner(), eClass);
		if (eClass.isInstance(parent))
			return parent;
		return eClass.isInstance(reference) ? (T) reference : null;
	}

	public static String getTokenFromQName(String qualifiedName) {
		return qualifiedName.replaceAll(NamedElement.SEPARATOR, "_");
	}

	public static String getDescription(Element element) {
		return ElementUtils.getComments(element);
	}
	
	public static String getTypeName(Type type) {
		if (type == null)
			return "<any>";
		if (type.getName() != null)
			return type.getName();
		if (type instanceof Behavior)
			return computeSignatureName(type);
		if (MDDExtensionUtils.isSignature(type))
			return computeSignatureName(type);
		return "Unknown " + type.eClass().getName();
	}
	
	public static String computeSignatureName(List<Parameter> signature) {
		StringBuffer name = new StringBuffer("{(");
		final List<Parameter> inputParameters =
						FeatureUtils.filterParameters(signature, ParameterDirectionKind.IN_LITERAL);
		for (Parameter parameter : inputParameters) {
			if (parameter.getName() != null) {
				name.append(parameter.getName());
				name.append(" ");
			}
			name.append(": ");
			final Type parameterType = parameter.getType();
			final String parameterTypeName = parameterType == null ? "any" : parameterType.getQualifiedName();
			name.append(parameterTypeName);
			addMultiplicity(name, parameter);
			name.append(", ");
		}
		if (!inputParameters.isEmpty())
			name.delete(name.length() - ", ".length(), name.length());
		name.append(")");
		List<Parameter> returnParameter =
						FeatureUtils.filterParameters(signature, ParameterDirectionKind.RETURN_LITERAL);
		if (!returnParameter.isEmpty()) {
			name.append(" : ");
			final Type returnType = returnParameter.get(0).getType();
			final String returnTypeName = returnType == null ? "any" : returnType.getQualifiedName();
			name.append(returnTypeName);
		}
		name.append('}');
		return name.toString();
	}

	public static String computeSignatureName(Type type) {
		if (type instanceof Behavior)
			return computeSignatureName(((Behavior) type).getOwnedParameters());
		if (MDDExtensionUtils.isSignature(type))
			return computeSignatureName(MDDExtensionUtils.getSignatureParameters(type));
		return null;
	}


	/**
	 * Returns whether the given name is qualified.
	 */
	public static boolean isQualifiedName(String name) {
		return name.indexOf(NamedElement.SEPARATOR) >= 0;
	}

	public static String removeFirstSegment(String qualifiedName) {
		int firstSeparator = qualifiedName.indexOf(NamedElement.SEPARATOR);
		return firstSeparator == -1 || firstSeparator == qualifiedName.length() - NamedElement.SEPARATOR.length()
						? null : qualifiedName.substring(firstSeparator + Namespace.SEPARATOR.length());
	}

	/**
	 * Returns the given qualified name with the last segment removed.
	 */
	public static String removeLastSegment(String qualifiedName) {
		int lastSeparator = qualifiedName.lastIndexOf(NamedElement.SEPARATOR);
		return lastSeparator == -1 ? null : qualifiedName.substring(0, lastSeparator);
	}

	public static String toShortString(Element element) {
		return element.eClass().getName() + "@" + System.identityHashCode(element);
	}

	/*
	 * This method dynamically invokes a corresponding visitor method based on the type of the node 
	 * being visited. If a type-specific method is not found (visitFoo(Foo), where Foo is the instance class of 
	 * the element), a generic visitAny(ActivityNode) method is invoked.  
	 */
	private static IActivityNodeTreeVisitor.Outcome visitNode(ActivityNode node, IActivityNodeTreeVisitor visitor) {
		String eClassName = node.eClass().getName();
		try {
			Method visitorMethod;
			try {
				visitorMethod =
								visitor.getClass().getDeclaredMethod("visit" + eClassName,
												node.eClass().getInstanceClass());
			} catch (NoSuchMethodException e) {
				// fallback: call visitAny(ActitivityNode) instead
				visitorMethod = visitor.getClass().getDeclaredMethod("visitAny", ActivityNode.class);
			}
			visitorMethod.setAccessible(true);
			return (Outcome) visitorMethod.invoke(visitor, node);
		} catch (NoSuchMethodException e) {
			// keep going...
		} catch (IllegalAccessException e) {
			LogUtils.logWarning(MDDCore.PLUGIN_ID, "Unexpected error", e);
		} catch (InvocationTargetException e) {
			LogUtils.logWarning(MDDCore.PLUGIN_ID, "Unexpected error", e);
		}
		// no visitor method found, continue
		return IActivityNodeTreeVisitor.Outcome.CONTINUE;
	}

	public static boolean visitTree(ActivityNode root, IActivityNodeTreeVisitor visitor) {
		final Outcome outcome = visitNode(root, visitor);
		if (outcome == Outcome.STOP)
			return false;
		if (outcome == Outcome.SKIP || !(root instanceof StructuredActivityNode))
			return true;
		StructuredActivityNode parent = (StructuredActivityNode) root;
		for (ActivityNode current : parent.getNodes())
			if (!visitTree(current, visitor))
				return false;
		return true;
	}

	private MDDUtil() {
		// prevent instantiation
	}

	public static boolean isGenerated(java.net.URI uri) {
		if (cachedParserFactory == null) {
			cachedParserFactory = SAXParserFactory.newInstance();
		}
		SAXParser xmlParser;
		try {
			xmlParser = cachedParserFactory.newSAXParser();
		} catch (ParserConfigurationException e) {
			if (Platform.inDebugMode())
				LogUtils.logError(MDDCore.PLUGIN_ID, "Error creating XML parser", e);
			return false;
		} catch (SAXException e) {
			if (Platform.inDebugMode())
				LogUtils.logError(MDDCore.PLUGIN_ID, "Error creating XML parser", e);
			return false;
		}
		final boolean[] generated = { false };
		final boolean[] aborted = { false };
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(uri.toURL().openStream());
			xmlParser.parse(stream, new DefaultHandler() {
				private boolean skipping = true;

				@Override
				public void startElement(String uri, String localName, String name, Attributes attributes)
								throws SAXException {
					if (name.equalsIgnoreCase("eAnnotations"))
						if (GENERATED.equals(attributes.getValue("source"))) {
							generated[0] = true;
							aborted[0] = true;
							throw new SAXParseException("", null);
						} else
							return;
					if (!skipping) {
						// should have seen the annotation by now
						aborted[0] = true;
						throw new SAXParseException("", null);
					}
					if (name.startsWith("uml"))
						skipping = false;
				}
			});
		} catch (SAXException e) {
			if (!aborted[0] && Platform.inDebugMode())
				LogUtils.logError(MDDCore.PLUGIN_ID, "Error parsing " + uri, e);
		} catch (IOException e) {
			if (Platform.inDebugMode())
				LogUtils.logError(MDDCore.PLUGIN_ID, "Error parsing " + uri, e);
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					// no biggie
				}
		}
		return generated[0];
	}

	public static boolean isGenerated(Package package_) {
		return getRootPackage(package_).getEAnnotation(GENERATED) != null;
	}
	
	public static Package getRootPackage(Package start) {
		final Package nestingPackage = start.getNestingPackage();
		return (nestingPackage != null) ? getRootPackage(nestingPackage) : start;
	}
	
	public static <T extends Element> List<T> findAllFrom(EObjectCondition condition, Collection<? extends Element> startingPoints) {
		List<T> result = new ArrayList<T>();
		IQueryResult partial = new SELECT(new FROM(startingPoints), new WHERE(condition)).execute();
		for (EObject object : partial)
			result.add((T) object);
		return result;
	}
	
	public static void markGenerated(Package package_) {
		if (MDDUtil.isGenerated(package_))
			return;
		final Package root = getRootPackage(package_);
		root.createEAnnotation(MDDUtil.GENERATED).getDetails().put("dateCreated", new SimpleDateFormat("yyyy/MM/dd hh:mm:ss SSS Z").format(new Date()));
	}
	
	public static String getGeneratedTimestamp(Package package_) {
		final Package root = getRootPackage(package_);
		EAnnotation eAnnotation = root.getEAnnotation(MDDUtil.GENERATED);
		if (eAnnotation == null)
			return null;
		return eAnnotation.getDetails().get("dateCreated");
	}
	
	public static void clearGenerated(Package package_) {
		Package root = getRootPackage(package_);
		EAnnotation annotation = root.getEAnnotation(GENERATED);
		if (annotation != null)
			root.getEAnnotations().remove(annotation);
	}
	
	public static void unloadResources(ResourceSet resourceSet) {
		unloadResources(resourceSet.getResources());
	}

	public static void unloadResources(EList<Resource> resources) {
		for (Resource resource : resources)
			unloadResource(resource);
	}

	public static void unloadResource(Resource current) {
		current.unload();
	}
	
	public static Properties loadRepositoryProperties(URI baseRepositoryURI) {
		URI propertiesURI = baseRepositoryURI.appendSegment(IRepository.MDD_PROPERTIES);
		URL asURL;
		try {
			asURL = new URL(propertiesURI.toString());
		} catch (MalformedURLException e) {
			// should never happen as it is produced by URI
			LogUtils.log(new Status(IStatus.ERROR, MDDCore.PLUGIN_ID, 0, "Error loading properties at '"
							+ propertiesURI + "'", e));
			return new Properties();
		}
		BufferedInputStream contents = null;
		try {
			contents = new BufferedInputStream(asURL.openStream());
			Properties properties = new Properties();
			properties.load(contents);
			return properties;
		} catch (FileNotFoundException e) {
			return new Properties();
		} catch (IOException e) {
			LogUtils.log(new Status(IStatus.ERROR, MDDCore.PLUGIN_ID, 0, "Error loading properties at '"
							+ propertiesURI + "'", e));
			return new Properties();
		} finally {
			IOUtils.closeQuietly(contents);
		}
	}

	public static boolean doesRepositoryExist(URI repositoryURI) {
		if (!repositoryURI.isFile())
			return false;
		File repositoryDir = new File(repositoryURI.toFileString());
		return repositoryDir.isDirectory() && new File(repositoryDir, IRepository.MDD_PROPERTIES).isFile();
	}
}