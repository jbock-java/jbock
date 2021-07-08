package net.jbock.processor;

import javax.inject.Scope;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Initial processor scope,
 * contains {@link javax.lang.model.util.Elements Elements}
 * and {@link javax.lang.model.util.Types Types}.
 */
@Scope
@Retention(RUNTIME)
@interface ProcessorScope {
}
