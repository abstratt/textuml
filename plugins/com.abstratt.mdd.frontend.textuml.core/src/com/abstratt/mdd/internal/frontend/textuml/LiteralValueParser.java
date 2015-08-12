package com.abstratt.mdd.internal.frontend.textuml;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.util.BasicTypeUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.core.util.MDDUtil;
import com.abstratt.mdd.core.util.TypeUtils;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;
import com.abstratt.mdd.frontend.core.spi.AbortedStatementCompilationException;
import com.abstratt.mdd.frontend.core.spi.ProblemBuilder;
import com.abstratt.mdd.frontend.textuml.grammar.analysis.DepthFirstAdapter;
import com.abstratt.mdd.frontend.textuml.grammar.node.Node;
import com.abstratt.mdd.frontend.textuml.grammar.node.TFalse;
import com.abstratt.mdd.frontend.textuml.grammar.node.TInteger;
import com.abstratt.mdd.frontend.textuml.grammar.node.TNull;
import com.abstratt.mdd.frontend.textuml.grammar.node.TReal;
import com.abstratt.mdd.frontend.textuml.grammar.node.TString;
import com.abstratt.mdd.frontend.textuml.grammar.node.TTrue;

public class LiteralValueParser {
	public static ValueSpecification parseLiteralValue(Node node, final org.eclipse.uml2.uml.Package currentPackage,
	        final ProblemBuilder<Node> problemBuilder) {
		final ValueSpecification[] result = { null };
		node.apply(new DepthFirstAdapter() {

			@Override
			public void caseTFalse(TFalse node) {
				final Classifier booleanType = findBuiltInType(problemBuilder, "Boolean", node);
				result[0] = MDDExtensionUtils.buildBasicValue(currentPackage, booleanType, Boolean.FALSE.toString());
			}

			@Override
			public void caseTInteger(TInteger node) {
				Classifier integerType = findBuiltInType(problemBuilder, "Integer", node);
				result[0] = MDDExtensionUtils.buildBasicValue(currentPackage, integerType, node.getText());
			}

			@Override
			public void caseTNull(TNull node) {
				result[0] = MDDUtil.createLiteralNull(currentPackage);
				result[0].setType(findBuiltInType(problemBuilder, TypeUtils.NULL_TYPE, node));
			}

			@Override
			public void caseTReal(TReal node) {
				final Classifier doubleType = findBuiltInType(problemBuilder, "Double", node);
				result[0] = MDDExtensionUtils.buildBasicValue(currentPackage, doubleType, node.getText());
			}

			@Override
			public void caseTString(TString node) {
				final Classifier stringType = findBuiltInType(problemBuilder, "String", node);
				String text = node.getText();
				result[0] = MDDExtensionUtils.buildBasicValue(currentPackage, stringType,
				        text.substring(1, text.length() - 1));
			}

			@Override
			public void caseTTrue(TTrue node) {
				final Classifier booleanType = findBuiltInType(problemBuilder, "Boolean", node);
				result[0] = MDDExtensionUtils.buildBasicValue(currentPackage, booleanType, Boolean.TRUE.toString());
			}
		});
		return result[0];
	}

	private static Classifier findBuiltInType(ProblemBuilder<Node> problemBuilder, String typeName, Node node) {
		// try first to find a type in the base package
		Classifier builtInType = BasicTypeUtils.findBuiltInType(typeName);
		if (builtInType == null) {
			problemBuilder.addProblem(new UnresolvedSymbol(typeName), node);
			throw new AbortedStatementCompilationException();
		}
		return builtInType;
	}
}
