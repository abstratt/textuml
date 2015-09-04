package com.abstratt.mdd.frontend.core;

import com.abstratt.mdd.core.Problem;

public class IdsShouldBeRequiredSingle extends Problem {

    public IdsShouldBeRequiredSingle() {
        super(Severity.WARNING);
    }

    @Override
    public String getMessage() {
        return "Id properties should have upper and lower bound equal to 1";
    }

}
