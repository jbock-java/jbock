package net.jbock.writing;

import jakarta.inject.Scope;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Scope
@Retention(RUNTIME)
@interface WritingScope {
}