package com.abstratt.resman;

public class ResourceException extends RuntimeException {
    public ResourceException(Exception e) {
        super(e);
    }

    public ResourceException(String string) {
        super(string);
    }

    private static final long serialVersionUID = 1L;
}
