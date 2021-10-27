package net.jbock.validate;

import jakarta.inject.Scope;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This scope creates a {@link CommandProcessor}.
 */
@Scope
@Retention(RUNTIME)
public @interface ValidateScope {
}