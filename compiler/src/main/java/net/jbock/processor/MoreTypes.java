package net.jbock.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static javax.lang.model.type.TypeKind.ARRAY;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.EXECUTABLE;
import static javax.lang.model.type.TypeKind.INTERSECTION;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static javax.lang.model.type.TypeKind.WILDCARD;

final class MoreTypes {

    private static final int HASH_SEED = 17;
    private static final int HASH_MULTIPLIER = 31;

    private MoreTypes() {
    }

    @SuppressWarnings("TypeEquals")
    private static boolean equal(
            TypeMirror a, TypeMirror b, Set<ComparedElements> visiting) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        // TypeMirror.equals is not guaranteed to return true for types that are equal, but we can
        // assume that if it does return true then the types are equal. This check also avoids getting
        // stuck in infinite recursion when Eclipse decrees that the upper bound of the second K in
        // <K extends Comparable<K>> is a distinct but equal K.
        // The javac implementation of ExecutableType, at least in some versions, does not take thrown
        // exceptions into account in its equals implementation, so avoid this optimization for
        // ExecutableType.
        @SuppressWarnings("TypesEquals")
        boolean equal = a.equals(b);
        if (equal && !(a instanceof ExecutableType)) {
            return true;
        }
        EqualVisitorParam p = new EqualVisitorParam();
        p.type = b;
        p.visiting = visiting;
        return a.accept(EqualVisitor.INSTANCE, p);
    }

    /**
     * Returns the type of the innermost enclosing instance, or null if there is none. This is the
     * same as {@link DeclaredType#getEnclosingType()} except that it returns null rather than
     * NoType for a static type. We need this because of
     * <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=508222">this bug</a> whereby
     * the Eclipse compiler returns a value for static classes that is not NoType.
     */
    private static TypeMirror enclosingType(DeclaredType t) {
        TypeMirror enclosing = t.getEnclosingType();
        if (enclosing.getKind().equals(TypeKind.NONE)
                || t.asElement().getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        return enclosing;
    }

    private static boolean equalLists(
            List<? extends TypeMirror> a, List<? extends TypeMirror> b, Set<ComparedElements> visiting) {
        int size = a.size();
        if (size != b.size()) {
            return false;
        }
        // Use iterators in case the Lists aren't RandomAccess
        Iterator<? extends TypeMirror> aIterator = a.iterator();
        Iterator<? extends TypeMirror> bIterator = b.iterator();
        while (aIterator.hasNext()) {
            // We checked that the lists have the same size, so we know that bIterator.hasNext() too.
            TypeMirror nextMirrorA = aIterator.next();
            TypeMirror nextMirrorB = bIterator.next();
            if (!equal(nextMirrorA, nextMirrorB, visiting)) {
                return false;
            }
        }
        return true;
    }

    private static int hash(TypeMirror mirror, Set<Element> visiting) {
        return mirror == null ? 0 : mirror.accept(HashVisitor.INSTANCE, visiting);
    }

    private static int hashList(List<? extends TypeMirror> mirrors, Set<Element> visiting) {
        int result = HASH_SEED;
        for (TypeMirror mirror : mirrors) {
            result *= HASH_MULTIPLIER;
            result += hash(mirror, visiting);
        }
        return result;
    }

    static TypeElement asTypeElement(TypeMirror mirror) {
        return MoreElements.asType(asElement(mirror));
    }

    /**
     * An alternate implementation of {@link Types#asElement} that does not require a {@link Types}
     * instance with the notable difference that it will throw {@link IllegalArgumentException}
     * instead of returning null if the {@link TypeMirror} can not be converted to an {@link Element}.
     *
     * @throws NullPointerException if {@code typeMirror} is {@code null}
     * @throws IllegalArgumentException if {@code typeMirror} cannot be converted to an {@link
     *     Element}
     */
    static Element asElement(TypeMirror typeMirror) {
        return typeMirror.accept(MoreTypes.AsElementVisitor.INSTANCE, null);
    }

    /**
     * Returns an {@link Equivalence} that can be used to compare types. The standard way to compare
     * types is {@link javax.lang.model.util.Types#isSameType Types.isSameType}, but this alternative
     * may be preferred in a number of cases:
     *
     * <ul>
     * <li>If you don't have an instance of {@code Types}.
     * <li>If you want a reliable {@code hashCode()} for the types, for example to construct a set
     *     of types using {@link java.util.HashSet} with {@link Equivalence#wrap(Object)}.
     * <li>If you want distinct type variables to be considered equal if they have the same names
     *     and bounds.
     * <li>If you want wildcard types to compare equal if they have the same bounds. {@code
     *     Types.isSameType} never considers wildcards equal, even when comparing a type to itself.
     * </ul>
     */
    public static Equivalence<TypeMirror> equivalence() {
        return TypeEquivalence.INSTANCE;
    }

    /**
     * Returns true if the raw type underlying the given {@link TypeMirror} represents the same raw
     * type as the given {@link Class} and throws an IllegalArgumentException if the {@link
     * TypeMirror} does not represent a type that can be referenced by a {@link Class}
     */
    public static boolean isTypeOf(final Class<?> clazz, TypeMirror type) {
        Objects.requireNonNull(clazz);
        return type.accept(new IsTypeOf(clazz), null);
    }

    /**
     * Returns a {@link ArrayType} if the {@link TypeMirror} represents an array or throws an {@link
     * IllegalArgumentException}.
     */
    public static ArrayType asArray(TypeMirror maybeArrayType) {
        return maybeArrayType.accept(ArrayTypeVisitor.INSTANCE, null);
    }

    private static final class TypeEquivalence extends Equivalence<TypeMirror> {
        private static final TypeEquivalence INSTANCE = new TypeEquivalence();

        @Override
        protected boolean doEquivalent(TypeMirror a, TypeMirror b) {
            return equal(a, b, Set.of());
        }

        @Override
        protected int doHash(TypeMirror t) {
            return MoreTypes.hash(t, Set.of());
        }

        @Override
        public String toString() {
            return "MoreTypes.equivalence()";
        }
    }

    // So EQUAL_VISITOR can be a singleton, we maintain visiting state, in particular which types
    // have been seen already, in this object.
    // The logic for handling recursive types like Comparable<T extends Comparable<T>> is very tricky.
    // If we're not careful we'll end up with an infinite recursion. So we record the types that
    // we've already seen during the recursion, and if we see the same pair of types again we just
    // return true provisionally. But "the same pair of types" is itself poorly-defined. We can't
    // just say that it is an equal pair of TypeMirrors, because of course if we knew how to
    // determine that then we wouldn't need the complicated type visitor at all. On the other hand,
    // we can't say that it is an identical pair of TypeMirrors either, because there's no
    // guarantee that the TypeMirrors for the two Ts in Comparable<T extends Comparable<T>> will be
    // represented by the same object, and indeed with the Eclipse compiler they aren't. We could
    // compare the corresponding Elements, since equality is well-defined there, but that's not enough
    // either, because the Element for Set<Object> is the same as the one for Set<String>. So we
    // approximate by comparing the Elements and, if there are any type arguments, requiring them to
    // be identical. This may not be foolproof either but it is sufficient for all the cases we've
    // encountered so far.
    private static final class EqualVisitorParam {
        TypeMirror type;
        Set<ComparedElements> visiting;
    }

    private static class ComparedElements {
        final Element a;
        final List<TypeMirror> aArguments;
        final Element b;
        final List<TypeMirror> bArguments;

        ComparedElements(
                Element a,
                List<TypeMirror> aArguments,
                Element b,
                List<TypeMirror> bArguments) {
            this.a = a;
            this.aArguments = aArguments;
            this.b = b;
            this.bArguments = bArguments;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ComparedElements) {
                ComparedElements that = (ComparedElements) o;
                int nArguments = aArguments.size();
                if (!this.a.equals(that.a) || !this.b.equals(that.b) || nArguments != bArguments.size()) {
                    // The arguments must be the same size, but we check anyway.
                    return false;
                }
                for (int i = 0; i < nArguments; i++) {
                    if (aArguments.get(i) != bArguments.get(i)) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return a.hashCode() * 31 + b.hashCode();
        }
    }

    private static final class EqualVisitor extends SimpleTypeVisitor8<Boolean, EqualVisitorParam> {
        private static final EqualVisitor INSTANCE = new EqualVisitor();

        @Override
        protected Boolean defaultAction(TypeMirror a, EqualVisitorParam p) {
            return a.getKind().equals(p.type.getKind());
        }

        @Override
        public Boolean visitArray(ArrayType a, EqualVisitorParam p) {
            if (p.type.getKind().equals(ARRAY)) {
                ArrayType b = (ArrayType) p.type;
                return equal(a.getComponentType(), b.getComponentType(), p.visiting);
            }
            return false;
        }

        @Override
        public Boolean visitDeclared(DeclaredType a, EqualVisitorParam p) {
            if (p.type.getKind().equals(DECLARED)) {
                DeclaredType b = (DeclaredType) p.type;
                Element aElement = a.asElement();
                Element bElement = b.asElement();
                Set<ComparedElements> newVisiting =
                        visitingSetPlus(
                                p.visiting, aElement, a.getTypeArguments(), bElement, b.getTypeArguments());
                if (newVisiting.equals(p.visiting)) {
                    // We're already visiting this pair of elements.
                    // This can happen for example with Enum in Enum<E extends Enum<E>>. Return a
                    // provisional true value since if the Elements are not in fact equal the original
                    // visitor of Enum will discover that. We have to check both Elements being compared
                    // though to avoid missing the fact that one of the types being compared
                    // differs at exactly this point.
                    return true;
                }
                return aElement.equals(bElement)
                        && equal(enclosingType(a), enclosingType(b), newVisiting)
                        && equalLists(a.getTypeArguments(), b.getTypeArguments(), newVisiting);
            }
            return false;
        }

        @Override
        @SuppressWarnings("TypeEquals")
        public Boolean visitError(ErrorType a, EqualVisitorParam p) {
            return a.equals(p.type);
        }

        @Override
        public Boolean visitExecutable(ExecutableType a, EqualVisitorParam p) {
            if (p.type.getKind().equals(EXECUTABLE)) {
                ExecutableType b = (ExecutableType) p.type;
                return equalLists(a.getParameterTypes(), b.getParameterTypes(), p.visiting)
                        && equal(a.getReturnType(), b.getReturnType(), p.visiting)
                        && equalLists(a.getThrownTypes(), b.getThrownTypes(), p.visiting)
                        && equalLists(a.getTypeVariables(), b.getTypeVariables(), p.visiting);
            }
            return false;
        }

        @Override
        public Boolean visitIntersection(IntersectionType a, EqualVisitorParam p) {
            if (p.type.getKind().equals(INTERSECTION)) {
                IntersectionType b = (IntersectionType) p.type;
                return equalLists(a.getBounds(), b.getBounds(), p.visiting);
            }
            return false;
        }

        @Override
        public Boolean visitTypeVariable(TypeVariable a, EqualVisitorParam p) {
            if (p.type.getKind().equals(TYPEVAR)) {
                TypeVariable b = (TypeVariable) p.type;
                TypeParameterElement aElement = (TypeParameterElement) a.asElement();
                TypeParameterElement bElement = (TypeParameterElement) b.asElement();
                Set<ComparedElements> newVisiting = visitingSetPlus(p.visiting, aElement, bElement);
                if (newVisiting.equals(p.visiting)) {
                    // We're already visiting this pair of elements.
                    // This can happen with our friend Eclipse when looking at <T extends Comparable<T>>.
                    // It incorrectly reports the upper bound of T as T itself.
                    return true;
                }
                // We use aElement.getBounds() instead of a.getUpperBound() to avoid having to deal with
                // the different way intersection types (like <T extends Number & Comparable<T>>) are
                // represented before and after Java 8. We do have an issue that this code may consider
                // that <T extends Foo & Bar> is different from <T extends Bar & Foo>, but it's very
                // hard to avoid that, and not likely to be much of a problem in practice.
                return equalLists(aElement.getBounds(), bElement.getBounds(), newVisiting)
                        && equal(a.getLowerBound(), b.getLowerBound(), newVisiting)
                        && a.asElement().getSimpleName().equals(b.asElement().getSimpleName());
            }
            return false;
        }

        @Override
        public Boolean visitWildcard(WildcardType a, EqualVisitorParam p) {
            if (p.type.getKind().equals(WILDCARD)) {
                WildcardType b = (WildcardType) p.type;
                return equal(a.getExtendsBound(), b.getExtendsBound(), p.visiting)
                        && equal(a.getSuperBound(), b.getSuperBound(), p.visiting);
            }
            return false;
        }

        @Override
        public Boolean visitUnknown(TypeMirror a, EqualVisitorParam p) {
            throw new UnsupportedOperationException();
        }

        private Set<ComparedElements> visitingSetPlus(
                Set<ComparedElements> visiting, Element a, Element b) {
            List<TypeMirror> noArguments = List.of();
            return visitingSetPlus(visiting, a, noArguments, b, noArguments);
        }

        private Set<ComparedElements> visitingSetPlus(
                Set<ComparedElements> visiting,
                Element a,
                List<? extends TypeMirror> aArguments,
                Element b,
                List<? extends TypeMirror> bArguments) {
            ComparedElements comparedElements =
                    new ComparedElements(
                            a, new ArrayList<>(aArguments),
                            b, new ArrayList<>(bArguments));
            Set<ComparedElements> newVisiting = new HashSet<>(visiting);
            newVisiting.add(comparedElements);
            return newVisiting;
        }
    }

    private static final class HashVisitor extends SimpleTypeVisitor8<Integer, Set<Element>> {
        private static final HashVisitor INSTANCE = new HashVisitor();

        int hashKind(int seed, TypeMirror t) {
            int result = seed * HASH_MULTIPLIER;
            result += t.getKind().hashCode();
            return result;
        }

        @Override
        protected Integer defaultAction(TypeMirror e, Set<Element> visiting) {
            return hashKind(HASH_SEED, e);
        }

        @Override
        public Integer visitArray(ArrayType t, Set<Element> visiting) {
            int result = hashKind(HASH_SEED, t);
            result *= HASH_MULTIPLIER;
            result += t.getComponentType().accept(this, visiting);
            return result;
        }

        @Override
        public Integer visitDeclared(DeclaredType t, Set<Element> visiting) {
            Element element = t.asElement();
            if (visiting.contains(element)) {
                return 0;
            }
            Set<Element> newVisiting = new HashSet<Element>(visiting);
            newVisiting.add(element);
            int result = hashKind(HASH_SEED, t);
            result *= HASH_MULTIPLIER;
            result += t.asElement().hashCode();
            result *= HASH_MULTIPLIER;
            result += t.getEnclosingType().accept(this, newVisiting);
            result *= HASH_MULTIPLIER;
            result += hashList(t.getTypeArguments(), newVisiting);
            return result;
        }

        @Override
        public Integer visitExecutable(ExecutableType t, Set<Element> visiting) {
            int result = hashKind(HASH_SEED, t);
            result *= HASH_MULTIPLIER;
            result += hashList(t.getParameterTypes(), visiting);
            result *= HASH_MULTIPLIER;
            result += t.getReturnType().accept(this, visiting);
            result *= HASH_MULTIPLIER;
            result += hashList(t.getThrownTypes(), visiting);
            result *= HASH_MULTIPLIER;
            result += hashList(t.getTypeVariables(), visiting);
            return result;
        }

        @Override
        public Integer visitTypeVariable(TypeVariable t, Set<Element> visiting) {
            int result = hashKind(HASH_SEED, t);
            result *= HASH_MULTIPLIER;
            result += t.getLowerBound().accept(this, visiting);
            TypeParameterElement element = (TypeParameterElement) t.asElement();
            for (TypeMirror bound : element.getBounds()) {
                result *= HASH_MULTIPLIER;
                result += bound.accept(this, visiting);
            }
            return result;
        }

        @Override
        public Integer visitWildcard(WildcardType t, Set<Element> visiting) {
            int result = hashKind(HASH_SEED, t);
            result *= HASH_MULTIPLIER;
            result += (t.getExtendsBound() == null) ? 0 : t.getExtendsBound().accept(this, visiting);
            result *= HASH_MULTIPLIER;
            result += (t.getSuperBound() == null) ? 0 : t.getSuperBound().accept(this, visiting);
            return result;
        }

        @Override
        public Integer visitUnknown(TypeMirror t, Set<Element> visiting) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class IsTypeOf extends SimpleTypeVisitor8<Boolean, Void> {
        private final Class<?> clazz;

        IsTypeOf(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        protected Boolean defaultAction(TypeMirror type, Void ignored) {
            throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
        }

        @Override
        public Boolean visitNoType(NoType noType, Void p) {
            if (noType.getKind().equals(TypeKind.VOID)) {
                return clazz.equals(Void.TYPE);
            }
            throw new IllegalArgumentException(noType + " cannot be represented as a Class<?>.");
        }

        @Override
        public Boolean visitError(ErrorType errorType, Void p) {
            return false;
        }

        @Override
        public Boolean visitPrimitive(PrimitiveType type, Void p) {
            switch (type.getKind()) {
                case BOOLEAN:
                    return clazz.equals(Boolean.TYPE);
                case BYTE:
                    return clazz.equals(Byte.TYPE);
                case CHAR:
                    return clazz.equals(Character.TYPE);
                case DOUBLE:
                    return clazz.equals(Double.TYPE);
                case FLOAT:
                    return clazz.equals(Float.TYPE);
                case INT:
                    return clazz.equals(Integer.TYPE);
                case LONG:
                    return clazz.equals(Long.TYPE);
                case SHORT:
                    return clazz.equals(Short.TYPE);
                default:
                    throw new IllegalArgumentException(type + " cannot be represented as a Class<?>.");
            }
        }

        @Override
        public Boolean visitArray(ArrayType array, Void p) {
            return clazz.isArray() && isTypeOf(clazz.getComponentType(), array.getComponentType());
        }

        @Override
        public Boolean visitDeclared(DeclaredType type, Void ignored) {
            TypeElement typeElement = MoreElements.asType(type.asElement());
            return typeElement.getQualifiedName().contentEquals(clazz.getCanonicalName());
        }
    }

    private static final class ArrayTypeVisitor extends CastingTypeVisitor<ArrayType> {
        private static final ArrayTypeVisitor INSTANCE = new ArrayTypeVisitor();

        ArrayTypeVisitor() {
            super("array");
        }

        @Override
        public ArrayType visitArray(ArrayType type, Void ignore) {
            return type;
        }
    }


    private static final class AsElementVisitor extends SimpleTypeVisitor8<Element, Void> {
        private static final AsElementVisitor INSTANCE = new AsElementVisitor();

        @Override
        protected Element defaultAction(TypeMirror e, Void p) {
            throw new IllegalArgumentException(e + " cannot be converted to an Element");
        }

        @Override
        public Element visitDeclared(DeclaredType t, Void p) {
            return t.asElement();
        }

        @Override
        public Element visitError(ErrorType t, Void p) {
            return t.asElement();
        }

        @Override
        public Element visitTypeVariable(TypeVariable t, Void p) {
            return t.asElement();
        }
    }

    private abstract static class CastingTypeVisitor<T> extends SimpleTypeVisitor8<T, Void> {
        private final String label;

        CastingTypeVisitor(String label) {
            this.label = label;
        }

        @Override
        protected T defaultAction(TypeMirror e, Void v) {
            throw new IllegalArgumentException(e + " does not represent a " + label);
        }
    }
}
