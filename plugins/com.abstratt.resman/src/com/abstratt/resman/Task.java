package com.abstratt.resman;

public interface Task<S> {
    S run(Resource<?> resource);
}
