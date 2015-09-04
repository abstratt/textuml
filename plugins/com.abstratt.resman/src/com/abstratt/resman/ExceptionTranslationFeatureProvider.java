package com.abstratt.resman;

public interface ExceptionTranslationFeatureProvider extends FeatureProvider {
    Throwable translate(Throwable toTranslate);
}