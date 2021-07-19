package net.jbock.context;

import javax.inject.Scope;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is the final scope which is created after item validation has
 * succeeded. It is responsible for the actual code generation.
 *
 * @see GeneratedClass#define()
 */
@Scope
@Retention(RUNTIME)
@interface ContextScope {
}