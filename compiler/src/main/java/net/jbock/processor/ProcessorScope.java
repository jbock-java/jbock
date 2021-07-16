package net.jbock.processor;

import net.jbock.validate.ValidateModule;

import javax.inject.Scope;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Initial processor scope,
 * contains {@link javax.lang.model.util.Elements Elements}
 * and {@link javax.lang.model.util.Types Types}.
 *
 * <p>Its purpose is to create the {@link ValidateModule},
 * in {@link CommandProcessingStep}.
 */
@Scope
@Retention(RUNTIME)
@interface ProcessorScope {
}
