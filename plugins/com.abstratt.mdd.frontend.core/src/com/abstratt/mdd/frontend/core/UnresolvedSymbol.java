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
package com.abstratt.mdd.frontend.core;

import org.eclipse.emf.ecore.EClass;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.Problem;

public class UnresolvedSymbol extends Problem {

    private String symbol;
    private String symbolType;

    /**
     * Returns the name of the UML metaclass of the symbol that failed to
     * resolve.
     * 
     * @return the type of the symbol
     */
    public String getSymbolType() {
        return symbolType;
    }

    public UnresolvedSymbol(String symbol) {
        this(symbol, null);
    }

    public UnresolvedSymbol(String symbol, EClass eClass) {
        super(IProblem.Severity.ERROR);
        this.symbol = symbol;
        this.symbolType = eClass == null ? "symbol" : eClass.getName();
    }

    public String getMessage() {
        return "Unknown " + symbolType + ": " + symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
