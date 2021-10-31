/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.jbock.processor;

import java.io.Serializable;
import java.util.function.BiPredicate;

import static java.util.Objects.requireNonNull;

/**
 * A strategy for determining whether two instances are considered equivalent, and for computing
 * hash codes in a manner consistent with that equivalence. Two examples of equivalences are the
 * identity equivalence and the {@linkplain #equals "equals" equivalence}.
 *
 * @author Bob Lee
 * @author Ben Yu
 * @author Gregory Kick
 * @since 10.0 (<a href="https://github.com/google/guava/wiki/Compatibility">mostly
 *     source-compatible</a> since 4.0)
 */
abstract class Equivalence<T> implements BiPredicate<T, T> {
    /** Constructor for use by subclasses. */
    protected Equivalence() {
    }


    /**
     * Returns {@code true} if the given objects are considered equivalent.
     *
     * <p>This method describes an <i>equivalence relation</i> on object references, meaning that for
     * all references {@code x}, {@code y}, and {@code z} (any of which may be null):
     *
     * <ul>
     *   <li>{@code equivalent(x, x)} is true (<i>reflexive</i> property)
     *   <li>{@code equivalent(x, y)} and {@code equivalent(y, x)} each return the same result
     *       (<i>symmetric</i> property)
     *   <li>If {@code equivalent(x, y)} and {@code equivalent(y, z)} are both true, then {@code
     *       equivalent(x, z)} is also true (<i>transitive</i> property)
     * </ul>
     *
     * <p>Note that all calls to {@code equivalent(x, y)} are expected to return the same result as
     * long as neither {@code x} nor {@code y} is modified.
     */
    @Override
    public final boolean test(T a, T b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return doEquivalent(a, b);
    }

    /**
     * Implemented by the user to determine whether {@code a} and {@code b} are considered equivalent,
     * subject to the requirements specified in {@link #test}.
     *
     * <p>This method should not be called except by {@link #test}. When {@link #test}
     * calls this method, {@code a} and {@code b} are guaranteed to be distinct, non-null instances.
     *
     * @since 10.0 (previously, subclasses would override equivalent())
     */
    protected abstract boolean doEquivalent(T a, T b);

    /**
     * Returns a hash code for {@code t}.
     *
     * <p>The {@code hash} has the following properties:
     *
     * <ul>
     *   <li>It is <i>consistent</i>: for any reference {@code x}, multiple invocations of {@code
     *       hash(x}} consistently return the same value provided {@code x} remains unchanged
     *       according to the definition of the equivalence. The hash need not remain consistent from
     *       one execution of an application to another execution of the same application.
     *   <li>It is <i>distributable across equivalence</i>: for any references {@code x} and {@code
     *       y}, if {@code equivalent(x, y)}, then {@code hash(x) == hash(y)}. It is <i>not</i>
     *       necessary that the hash be distributable across <i>inequivalence</i>. If {@code
     *       equivalence(x, y)} is false, {@code hash(x) == hash(y)} may still be true.
     *   <li>{@code hash(null)} is {@code 0}.
     * </ul>
     */
    public final int hash(T t) {
        if (t == null) {
            return 0;
        }
        return doHash(t);
    }

    /**
     * Implemented by the user to return a hash code for {@code t}, subject to the requirements
     * specified in {@link #hash}.
     *
     * <p>This method should not be called except by {@link #hash}. When {@link #hash} calls this
     * method, {@code t} is guaranteed to be non-null.
     *
     * @since 10.0 (previously, subclasses would override hash())
     */
    protected abstract int doHash(T t);

    /**
     * Returns a wrapper of {@code reference} that implements {@link Wrapper#equals(Object)
     * Object.equals()} such that {@code wrap(a).equals(wrap(b))} if and only if {@code equivalent(a,
     * b)}.
     *
     * @since 10.0
     */
    public final <S extends T> Wrapper<S> wrap(S reference) {
        return new Wrapper<>(this, reference);
    }

    /**
     * Wraps an object so that {@link #equals(Object)} and {@link #hashCode()} delegate to an {@link
     * Equivalence}.
     *
     * <p>For example, given an {@link Equivalence} for {@link String strings} named {@code equiv}
     * that tests equivalence using their lengths:
     *
     * <pre>{@code
     * equiv.wrap("a").equals(equiv.wrap("b")) // true
     * equiv.wrap("a").equals(equiv.wrap("hello")) // false
     * }</pre>
     *
     * <p>Note in particular that an equivalence wrapper is never equal to the object it wraps.
     *
     * <pre>{@code
     * equiv.wrap(obj).equals(obj) // always false
     * }</pre>
     *
     * @since 10.0
     */
    public static final class Wrapper<T> implements Serializable {
        private final Equivalence<? super T> equivalence;
        private final T reference;

        private Wrapper(Equivalence<? super T> equivalence, T reference) {
            this.equivalence = requireNonNull(equivalence);
            this.reference = reference;
        }

        /** Returns the (possibly null) reference wrapped by this instance. */
        public T get() {
            return reference;
        }

        /**
         * Returns {@code true} if {@link Equivalence#test(Object, Object)} applied to the wrapped
         * references is {@code true} and both wrappers use the {@link Object#equals(Object) same}
         * equivalence.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Wrapper) {
                Wrapper<?> that = (Wrapper<?>) obj; // note: not necessarily a Wrapper<T>

                if (this.equivalence.equals(that.equivalence)) {
                    /*
                     * We'll accept that as sufficient "proof" that either equivalence should be able to
                     * handle either reference, so it's safe to circumvent compile-time type checking.
                     */
                    @SuppressWarnings("unchecked")
                    Equivalence<Object> equivalence = (Equivalence<Object>) this.equivalence;
                    return equivalence.test(this.reference, that.reference);
                }
            }
            return false;
        }

        /** Returns the result of {@link Equivalence#hash(Object)} applied to the wrapped reference. */
        @Override
        public int hashCode() {
            return equivalence.hash(reference);
        }

        /**
         * Returns a string representation for this equivalence wrapper. The form of this string
         * representation is not specified.
         */
        @Override
        public String toString() {
            return equivalence + ".wrap(" + reference + ")";
        }

        private static final long serialVersionUID = 0;
    }
}
