/** 
 * Copyright (c) 2006, 2008 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Rafael Chaves (Abstratt Technologies) - initial API and implementation
 * Vladimir Sosnin - #2798743
 */
package com.abstratt.mdd.frontend.textuml.renderer

import java.io.PrintWriter 
import java.util.Arrays
import java.util.List
import org.apache.commons.lang.StringUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Extension
import org.eclipse.uml2.uml.MultiplicityElement
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.Namespace
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.Property
import org.eclipse.uml2.uml.Stereotype
import org.eclipse.uml2.uml.Type
import org.eclipse.uml2.uml.TypedElement
import org.eclipse.uml2.uml.VisibilityKind
import org.eclipse.uml2.uml.util.UMLUtil
import com.abstratt.mdd.frontend.textuml.core.TextUMLConstants
import static extension com.abstratt.mdd.core.util.StateMachineUtils.* 
import org.eclipse.uml2.uml.StateMachine
import static extension com.abstratt.mdd.core.util.ActivityUtils.*
import static extension com.abstratt.mdd.core.util.MDDExtensionUtils.*
import org.eclipse.uml2.uml.Enumeration
import org.eclipse.uml2.uml.ValueSpecification
import org.eclipse.uml2.uml.Activity
import org.eclipse.uml2.uml.LiteralNull
import org.eclipse.uml2.uml.LiteralString
import com.abstratt.mdd.core.util.ActivityUtils
import org.eclipse.uml2.uml.Action
import com.abstratt.mdd.modelrenderer.IndentedPrintWriter
import org.eclipse.uml2.uml.ValueSpecificationAction
import org.eclipse.uml2.uml.LiteralUnlimitedNatural

class TextUMLRenderingUtils {
	def static String renderMultiplicity(MultiplicityElement multiple, boolean brackets) {
		if(!multiple.isMultivalued() && multiple.getLower() === 0) return ""
		var String constraints = ""
		if (multiple.isMultivalued()) {
			if(multiple.isOrdered() && multiple.isUnique()) constraints = "{ordered}" else if(multiple.isOrdered() &&
				!multiple.isUnique()) constraints = "{ordered, nonunique}" else if(!multiple.isOrdered() &&
				!multiple.isUnique()) constraints = "{nonunique}"
		}
		if (multiple.lowerBound() === multiple.upperBound()) {
			if (multiple.upperBound() === -1)
				return wrapInBrackets("*", brackets) + constraints
			else if (multiple.upperBound() !== 1) {
				return wrapInBrackets(Integer.toString(multiple.upperBound()), brackets) + constraints
			}
			return ""
		}
		var StringBuffer interval = new StringBuffer()
		interval.append(multiple.lowerBound())
		interval.append(", ")
		interval.append(if(multiple.upperBound() === -1) "*" else multiple.upperBound())
		return wrapInBrackets(interval.toString(), brackets) + constraints
	}

	def private static String wrapInBrackets(String original, boolean useBrackets) {
		return if (useBrackets) '''[«original»]''' else ''' «original»'''
	}

	def static String renderVisibility(VisibilityKind visibility) {
		if(visibility === null) return ""
		return '''«visibility.getName()» '''
	}

	def static void renderStereotypeApplications(PrintWriter writer, NamedElement element) {
		renderStereotypeApplications(writer, element, true)
	}

	def static void renderStereotypeApplications(PrintWriter writer, NamedElement element, boolean newLine) {
		var List<EObject> applications = element.getStereotypeApplications()
		if(applications.isEmpty()) return;
		var StringBuilder builder = new StringBuilder()
		builder.append("[")
		for (EObject application : applications) {
			renderStereotypeApplication(element, builder, application)
			builder.append(", ")
		}
		builder.delete(builder.length() - 2, builder.length())
		builder.append("]")
		writer.print(builder)
		if(newLine) writer.println() else writer.print(Character.valueOf(' ').charValue)
	}

	/** 
	 * Produces the name of the type of the typed element including
	 * multiplicity.
	 */
	def static String getQualifiedNameIfNeeded(TypedElement typedElement) {
		var StringBuilder builder = new StringBuilder()
		var String typeReference
		val Type type = typedElement.getType()
		val Namespace namespace = typedElement.getNamespace()
		typeReference = getQualifiedNameIfNeeded(type, namespace)
		builder.append(typeReference)
		if(typedElement instanceof MultiplicityElement) builder.append(
			TextUMLRenderingUtils.renderMultiplicity((typedElement as MultiplicityElement), true))
		return builder.toString()
	}

	def static String getQualifiedNameIfNeeded(Type type, Namespace namespace) {
		var String typeReference
		if (type !== null && type.getName() !== null) {
			typeReference = getQualifiedNameIfNeeded((type as NamedElement), namespace)
		} else
			typeReference = "any"
		return typeReference
	}

	def static String getQualifiedNameIfNeeded(NamedElement element, Namespace currentNamespace) {
		if(element.getName() === null) return "unnamed"
		if(currentNamespace === null) return qualifiedName(element)
		if(element.getNamespace() === currentNamespace) return name(element)
		if(element.getNamespace() instanceof Package &&
			currentNamespace.getImportedPackages().contains(element.getNamespace())) return name(element)
		if(currentNamespace.getImportedElements().contains(element)) return name(element)
		var Namespace parentNamespace = currentNamespace.getNamespace()
		return if(parentNamespace === null) qualifiedName(element) else getQualifiedNameIfNeeded(element,
			parentNamespace)
	}

	def private static void renderStereotypeApplication(NamedElement element, StringBuilder builder,
		EObject application) {
		var Stereotype stereotype = UMLUtil.getStereotype(application)
		builder.append(getQualifiedNameIfNeeded(stereotype, (application.eContainer() as Namespace)))
		builder.append("(")
		var int mark = builder.length()
		for (Property stereotypeProperty : stereotype.getAllAttributes()) {
			var Object taggedValue = element.getValue(stereotype, stereotypeProperty.getName())
			if (shouldRenderStereotypeProperty(element, stereotypeProperty, taggedValue)) {
				builder.append(name(stereotypeProperty))
				builder.append("=")
				var boolean isString = taggedValue instanceof String
				if(isString) builder.append(Character.valueOf('"').charValue)
				if(taggedValue instanceof NamedElement) taggedValue = ((taggedValue as NamedElement)).getName()
				builder.append(taggedValue)
				if(isString) builder.append(Character.valueOf('"').charValue)
				builder.append(",")
			}
		}
		if (builder.length() > mark) {
			builder.delete(builder.length() - 1, builder.length())
			builder.append(")")
		} else
			builder.delete(mark - 1, mark)
	}

	def private static boolean shouldRenderStereotypeProperty(NamedElement element, Property stereotypeProperty,
		Object taggedValue) {
		if(stereotypeProperty.getAssociation() instanceof Extension) return false
		if(taggedValue === null) return false
		if(taggedValue instanceof Element) if(!(taggedValue instanceof NamedElement)) return false
		// default value for required property of type Integer is 0
		// so don't show it
		if(taggedValue instanceof Integer && stereotypeProperty.getLower() >= 1 &&
			((taggedValue as Integer)).equals(0)) return false
		// don't show default values for stereotype properties
		var String defaultValue = stereotypeProperty.getDefault()
		if (defaultValue !== null && defaultValue.equals(taggedValue.toString())) {
			return false
		}
		return true
	}

	def private static String escapeIdentifierIfNeeded(String identifier) {
		if(StringUtils.isBlank(identifier)) return ""
		if (!StringUtils.isAlpha(identifier)) {
			var StringBuffer result = new StringBuffer(identifier.length())
			var char[] chars = identifier.toCharArray()
			for (var int i = 0; i < chars.length; i++) {
				if({
					val _rdIndx_chars = i
					chars.get(_rdIndx_chars)
				} !== Character.valueOf('_').charValue && ((Character.isDigit({
					val _rdIndx_chars = i
					chars.get(_rdIndx_chars)
				}) && i === 0) || !Character.isLetterOrDigit({
					val _rdIndx_chars = i
					chars.get(_rdIndx_chars)
				}))) result.append(Character.valueOf('\\').charValue)
				result.append({
					val _rdIndx_chars = i
					chars.get(_rdIndx_chars)
				})
			}
			return result.toString()
		}
		if(Arrays.binarySearch(TextUMLConstants.KEYWORDS, identifier) >= 0) return '''\«identifier»'''
		return identifier
	}

	def static String name(NamedElement named) {
		return escapeIdentifierIfNeeded(named.getName())
	}

	def static String qualifiedName(NamedElement named) {
		var StringBuffer qualifiedName = new StringBuffer(name(named))
		for (Namespace namespace : named.allNamespaces()) {
			var String namespaceName = name(namespace)
			if(namespaceName === null || namespaceName.isEmpty()) return null
			qualifiedName.insert(0, NamedElement.SEPARATOR)
			qualifiedName.insert(0, namespaceName)
		}
		return qualifiedName.toString()
	}
	
    def static generateDefaultValue(Type type) {
        switch (type) {
            StateMachine: '''«type.stateMachineContext».«type.name».«type.name».«type.initialVertex.name»'''
            Enumeration: '''«type.name».«type.ownedLiterals.head.name»'''
            org.eclipse.uml2.uml.Class:
                switch (type.name) {
                    case 'Boolean': 'false'
                    case 'Integer': '0'
                    case 'Double': '0'
                    case 'String': '""'
                    case 'Memo': '""'
                    default: '''TBD: «type.name»'''
                }
            default:
                null
        }
    }
    
    def static CharSequence generateActivity(Activity activity) {
    	new ActivityGenerator().generateActivity(activity)
    }
	
	def static CharSequence renderValue(ValueSpecificationAction valueSpec) {
	    valueSpec.value.renderValue()
    }
	
	def static CharSequence renderValue(ValueSpecification valueSpec) {
    	if (valueSpec.behaviorReference) {
    		val closure = valueSpec.resolveBehaviorReference as Activity
    		'''
    		(«closure.closureInputParameters.map[name].join(", ")») {
    			«closure.generateActivity.toString().trim()»
    		}'''
    		
    	} else switch (valueSpec) {
            LiteralNull : if (valueSpec.emptySet) '''«valueSpec.type.name»[]''' else 'null'
            LiteralString : switch (valueSpec.type.name) {
                case 'String': '''"«valueSpec.value»"'''
                default: valueSpec.value    
            }
            default: valueSpec.stringValue
        }
	}
	
}
