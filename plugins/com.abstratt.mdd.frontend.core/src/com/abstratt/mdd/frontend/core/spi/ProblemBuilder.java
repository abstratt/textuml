/*******************************************************************************
 * Copyright (c) 2006, 2010 Abstratt Technologies
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rafael Chaves (Abstratt Technologies) - initial API and implementation
 *******************************************************************************/
package com.abstratt.mdd.frontend.core.spi;

import java.util.function.Supplier;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.core.UnclassifiedProblem;

/** A problem builder builds problems based on node information. */
public class ProblemBuilder<N> {

    private IProblemTracker problems;
    private ISourceMiner<N> miner;

    public ProblemBuilder(IProblemTracker problems, ISourceMiner<N> miner) {
        this.problems = problems;
        this.miner = miner;
    }

    public void addError(String message, N node) {
        addProblem(new UnclassifiedProblem(message), node);
    }

    public void addProblem(IProblem problem, N node) {
        if (node != null)
            problem.setAttribute(IProblem.LINE_NUMBER, miner.getLineNumber(node));
        problems.add(problem);
    }

    public void addWarning(String message, N node) {
        addProblem(new UnclassifiedProblem(IProblem.Severity.WARNING, message), node);
    }

    public boolean hasErrors() {
        return problems.hasProblems(Severity.ERROR);
    }

    public void ensure(boolean condition, String message, N node) {
        if (!condition) {
            addError(message, node);
            throw new AbortedScopeCompilationException();
        }
    }
    
    public void ensure(boolean condition, N node, Supplier<IProblem> errorReporter) {
        if (!condition) {
            IProblem problem = errorReporter.get();
            addProblem(problem , node);
            if (problem.getSeverity() == Severity.ERROR) 
                throw new AbortedScopeCompilationException();
        }
    }
    
    public void ensure(boolean condition, N node, Severity severity, Supplier<String> messageProvider) {
        ensure(condition, node, () -> new UnclassifiedProblem(severity, messageProvider.get()));
    }
}
