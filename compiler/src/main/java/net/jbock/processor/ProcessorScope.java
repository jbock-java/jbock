package net.jbock.processor;

import jakarta.inject.Scope;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Scope
@Retention(RUNTIME)
@interface ProcessorScope {
}
