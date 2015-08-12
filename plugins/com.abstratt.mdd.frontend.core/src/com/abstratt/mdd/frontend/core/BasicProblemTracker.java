package com.abstratt.mdd.frontend.core;

import java.util.ArrayList;
import java.util.List;

import com.abstratt.mdd.core.IProblem;
import com.abstratt.mdd.core.IProblem.Severity;
import com.abstratt.mdd.frontend.core.spi.IProblemTracker;

public class BasicProblemTracker implements IProblemTracker {
    protected List<IProblem> problems = new ArrayList<IProblem>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.abstratt.mdd.core.frontend.spi.IProblemTracker#add(com.abstratt.mdd
     * .core.frontend.IProblem)
     */
    public void add(IProblem toReport) {
        problems.add(toReport);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.abstratt.mdd.core.frontend.spi.IProblemTracker#getAllProblems()
     */
    public IProblem[] getAllProblems() {
        return problems.toArray(new IProblem[problems.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.abstratt.mdd.core.frontend.spi.IProblemTracker#hasProblems(com.abstratt
     * .mdd.core.frontend.IProblem.Severity)
     */
    public boolean hasProblems(Severity minimumSeverity) {
        return ProblemUtils.hasProblems(this.problems, minimumSeverity);
    }
}
