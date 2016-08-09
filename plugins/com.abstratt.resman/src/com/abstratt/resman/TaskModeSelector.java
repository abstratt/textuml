package com.abstratt.resman;

/**
 * A generic feature type that helps determine the mode we are operating a
 * resource.
 */
public interface TaskModeSelector {
    Mode getMode();
    String getEnvironment();

    enum Mode {
        ReadOnly, ReadWrite
    }
}
