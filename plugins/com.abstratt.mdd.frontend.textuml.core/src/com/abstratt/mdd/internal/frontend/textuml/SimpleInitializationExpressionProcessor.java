package com.abstratt.mdd.internal.frontend.textuml;

import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.TypedElement;
import org.eclipse.uml2.uml.UMLPackage.Literals;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.IRepository;
import com.abstratt.mdd.core.Step;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.TypeMismatch;
import com.abstratt.mdd.frontend.core.UnknownType;
import com.abstratt.mdd.frontend.core.spi.AbortedScopeCompilationException;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.DeferredReference;
import com.abstratt.mdd.frontend.core.spi.IReferenceTracker;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.core.TextUMLCore;
import com.abstratt.mdd.frontend.textuml.grammar.node.AIdentifierLiteralOrIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.ALiteralLiteralOrIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.PLiteralOrIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.PTypeIdentifier;
import com.abstratt.mdd.frontend.textuml.grammar.node.TIdentifier;

public class SimpleInitializationExpressionProcessor {

    private IReferenceTracker referenceTracker;
    private ProblemBuilder<Node> problemBuilder;
    private Namespace currentNamespace;
    private IRepository repository;

    SimpleInitializationExpressionProcessor(SourceCompilationContext<Node> sourceContext, Namespace currentNamespace) {
        this.referenceTracker = sourceContext.getReferenceTracker();
        this.problemBuilder = sourceContext.getProblemBuilder();
        this.currentNamespace = currentNamespace;
        this.repository = sourceContext.getContext().getRepository();
    }

    protected ValueSpecification parseValueSpecification(PLiteralOrIdentifier node, final Type expectedType) {
        if (node instanceof ALiteralLiteralOrIdentifier) {
            ValueSpecification asLiteralValue = LiteralValueParser.parseLiteralValue(node,
                    currentNamespace.getNearestPackage(), problemBuilder);
            return asLiteralValue;
        }
        if (expectedType instanceof Enumeration)
            return parseEnumerationLiteral(node, expectedType);
        problemBuilder.addError("Enumeration or data type literal expected ", node);
        throw new AbortedStatementCompilationException();
    }

    protected ValueSpecification parseEnumerationLiteral(PLiteralOrIdentifier node, final Type expectedType) {
        TIdentifier identifier = ((AIdentifierLiteralOrIdentifier) node).getIdentifier();
        String literalName = identifier.getText().trim();
        Enumeration targetEnumeration = (Enumeration) expectedType;
        EnumerationLiteral enumerationValue = ((Enumeration) targetEnumeration).getOwnedLiteral(literalName);
        if (enumerationValue == null) {
            problemBuilder.addError(
                    "Unknown enumeration literal '" + literalName + "' in '" + targetEnumeration.getName() + "'", node);
            throw new AbortedScopeCompilationException();
        }
        InstanceValue valueSpec = (InstanceValue) currentNamespace.getNearestPackage().createPackagedElement(null,
                IRepository.PACKAGE.getInstanceValue());
        valueSpec.setInstance(enumerationValue);
        valueSpec.setType(targetEnumeration);
        return valueSpec;
    }

    public void process(final PTypeIdentifier typeIdentifierNode, final TypedElement typedElement,
            final PLiteralOrIdentifier initializationExpression) {
        String typeIdentifier = TextUMLCore.getSourceMiner().getQualifiedIdentifier(typeIdentifierNode);

        referenceTracker.add(new DeferredReference<Type>(typeIdentifier, Literals.TYPE, currentNamespace) {

            @Override
            protected void onBind(Type element) {
                if (element == null) {
                    problemBuilder.addProblem(new UnknownType(getSymbolName()), typeIdentifierNode);
                    return;
                }
                typedElement.setType(element);
                final ValueSpecification valueSpec = parseValueSpecification(initializationExpression,
                        typedElement.getType());
                if (valueSpec != null) {
                    if (!TypeUtils.isCompatible(repository, valueSpec, typedElement, null))
                        problemBuilder.addProblem(new TypeMismatch(typedElement.getType().getQualifiedName(), valueSpec
                                .getType().getQualifiedName()), typeIdentifierNode);
                    else {
                        if (typedElement instanceof Parameter)
                            ((Parameter) typedElement).setDefaultValue(valueSpec);
                        else if (typedElement instanceof Property)
                            ((Property) typedElement).setDefaultValue(valueSpec);
                        else
                            throw new IllegalArgumentException(typedElement.getQualifiedName() + " - "
                                    + typedElement.eClass().getName());
                    }
                }
            }
        }, Step.GENERAL_RESOLUTION);
    }

}
