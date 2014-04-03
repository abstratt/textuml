package com.abstratt.mdd.frontend.core.builder;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ValueSpecification;

import com.abstratt.mdd.core.util.BasicTypeUtils;
import com.abstratt.mdd.core.util.MDDExtensionUtils;
import com.abstratt.mdd.frontend.core.UnresolvedSymbol;

public class BasicTypeValueSpecificationBuilder extends ValueSpecificationBuilder<ValueSpecification> {

	private String value;
	private String type;
	
	public BasicTypeValueSpecificationBuilder(UML2ProductKind kind) {
		super(kind);
	}

	@Override
	protected ValueSpecification createProduct() {
		Classifier basicType = findBuiltInType(type);
		return MDDExtensionUtils.buildBasicValue(as(PackageBuilder.class).getProduct(), basicType, value);
	}
	
	private Classifier findBuiltInType(String typeName) {
		// try first to find a type in the base package
		Classifier builtInType = BasicTypeUtils.findBuiltInType(typeName);
		if (builtInType == null)
			abortStatement(new UnresolvedSymbol(
					typeName));
		return builtInType;
	}
	
	public BasicTypeValueSpecificationBuilder basicValue(String basicValue) {
		this.value = basicValue;
		return this;
	}
	public BasicTypeValueSpecificationBuilder basicType(String basicType) {
		this.type = basicType;
		return this;
	}

}
