package net.jbock.writing;

import jakarta.inject.Scope;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is the final scope which is created after validation of all annotated methods has
 * succeeded. It is responsible for the actual code generation.
 *
 * @see ParserClass#define()
 */
@Scope
@Retention(RUNTIME)
@interface WritingScope {
}