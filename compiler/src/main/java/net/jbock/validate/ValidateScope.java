package net.jbock.validate;

import javax.inject.Scope;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This is the main scope.
 * It creates {@link net.jbock.convert.Mapped}
 * items via {@link net.jbock.convert.ConvertModule}, and
 * passes these to the {@link net.jbock.context.ContextModule}
 * which generates the actual java code.
 *
 * <p>All of this happens in {@link CommandProcessor}.
 */
@Scope
@Retention(RUNTIME)
@interface ValidateScope {
}