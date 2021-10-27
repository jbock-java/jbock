package net.jbock.processor;

import jakarta.inject.Scope;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Outermost scope,
 * contains {@link javax.lang.model.util.Elements Elements}
 * and {@link javax.lang.model.util.Types Types}.
 */
@Scope
@Retention(RUNTIME)
@interface ProcessorScope {
}
