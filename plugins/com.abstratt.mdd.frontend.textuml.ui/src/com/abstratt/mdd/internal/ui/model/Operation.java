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
package com.abstratt.mdd.internal.ui.model;

import org.eclipse.swt.graphics.Image;

import com.abstratt.mdd.frontend.core.ASTNode;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationDecl;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationHeader;
import com.abstratt.mdd.internal.frontend.textuml.node.AOperationOperationKeyword;
import com.abstratt.mdd.internal.frontend.textuml.node.AOptionalReturnType;
import com.abstratt.mdd.internal.frontend.textuml.node.AQueryOperationKeyword;
import com.abstratt.mdd.internal.frontend.textuml.node.ASignature;
import com.abstratt.mdd.internal.frontend.textuml.node.POperationKeyword;
import com.abstratt.mdd.internal.frontend.textuml.node.POptionalReturnType;
import com.abstratt.mdd.internal.frontend.textuml.node.Token;
import com.abstratt.mdd.ui.Activator;
import com.abstratt.mdd.ui.UIConstants;

public class Operation extends LeafModelObject {

	public Operation(UIModelObject parent, ASTNode node) {
		super(parent, node);
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(UIConstants.ICON_OPERATION);
	}

	protected AOperationDecl getModel() {
		return (AOperationDecl) node.getBaseNode();
	}

	@Override
	public String getOriginalText() {
		AOperationDecl declaration = getModel();
		StringBuffer text = new StringBuffer();
		final AOperationHeader operationHeader = ((AOperationHeader) declaration.getOperationHeader());
		text.append(operationHeader.getIdentifier().getText());
		POptionalReturnType returnType = ((ASignature) operationHeader.getSignature()).getOptionalReturnType();
		text.append("() ");
		if (returnType instanceof AOptionalReturnType) {
			text.append(returnType);
		}
		return text.toString();
	}

	@Override
	public Token getToken() {
		AOperationHeader header = (AOperationHeader) getModel().getOperationHeader();
		POperationKeyword keyword = header.getOperationKeyword();
		return keyword instanceof AQueryOperationKeyword ? ((AQueryOperationKeyword) keyword).getQuery() : ((AOperationOperationKeyword) keyword).getOperation();
	}

}