package net.jbock.convert;

import net.jbock.context.ContextModule;

import javax.inject.Scope;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A per-item scope. Its purpose is to validate the item type, and
 * assign a converter. Finally it wraps the item in a {@link Mapped}
 * instance, to make the additional converter information available
 * for using it in the context scope.
 *
 * <p>This annotation must be public, because the convert package has
 * subpackages.
 *
 * @see ContextModule
 */
public @interface ConvertScope {
}