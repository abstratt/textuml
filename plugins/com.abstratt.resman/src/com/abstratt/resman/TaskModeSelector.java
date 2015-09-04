package com.abstratt.resman;

/**
 * A generic feature type that helps determine the mode we are operating a
 * resource.
 */
public interface TaskModeSelector {
    Mode getMode();

    enum Mode {
        ReadOnly, ReadWrite
    }
}
