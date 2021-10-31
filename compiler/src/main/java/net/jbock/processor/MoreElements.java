package net.jbock.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor8;

final class MoreElements {

    private MoreElements() {
    }

    private static final class ExecutableElementVisitor
            extends CastingElementVisitor<ExecutableElement> {
        private static final ExecutableElementVisitor INSTANCE = new ExecutableElementVisitor();

        ExecutableElementVisitor() {
            super("executable element");
        }

        @Override
        public ExecutableElement visitExecutable(ExecutableElement e, Void label) {
            return e;
        }
    }


    /**
     * Returns the given {@link Element} instance as {@link ExecutableElement}.
     *
     * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
     * always be used over that idiom as instructed in the documentation for {@link Element}.
     *
     * @throws NullPointerException if {@code element} is {@code null}
     * @throws IllegalArgumentException if {@code element} isn't a {@link ExecutableElement}.
     */
    static ExecutableElement asExecutable(Element element) {
        return element.accept(ExecutableElementVisitor.INSTANCE, null);
    }

    /**
     * Returns the given {@link Element} instance as {@link TypeElement}.
     *
     * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
     * always be used over that idiom as instructed in the documentation for {@link Element}.
     *
     * @throws NullPointerException if {@code element} is {@code null}
     * @throws IllegalArgumentException if {@code element} isn't a {@link TypeElement}.
     */
    static TypeElement asType(Element element) {
        return element.accept(TypeElementVisitor.INSTANCE, null);
    }

    private static final class PackageElementVisitor extends CastingElementVisitor<PackageElement> {
        private static final PackageElementVisitor INSTANCE = new PackageElementVisitor();

        PackageElementVisitor() {
            super("package element");
        }

        @Override
        public PackageElement visitPackage(PackageElement e, Void ignore) {
            return e;
        }
    }

    /**
     * Returns the given {@link Element} instance as {@link PackageElement}.
     *
     * <p>This method is functionally equivalent to an {@code instanceof} check and a cast, but should
     * always be used over that idiom as instructed in the documentation for {@link Element}.
     *
     * @throws NullPointerException if {@code element} is {@code null}
     * @throws IllegalArgumentException if {@code element} isn't a {@link PackageElement}.
     */
    static PackageElement asPackage(Element element) {
        return element.accept(PackageElementVisitor.INSTANCE, null);
    }

    private static final class TypeElementVisitor extends CastingElementVisitor<TypeElement> {
        private static final TypeElementVisitor INSTANCE = new TypeElementVisitor();

        TypeElementVisitor() {
            super("type element");
        }

        @Override
        public TypeElement visitType(TypeElement e, Void ignore) {
            return e;
        }
    }


    private abstract static class CastingElementVisitor<T> extends SimpleElementVisitor8<T, Void> {
        private final String label;

        CastingElementVisitor(String label) {
            this.label = label;
        }

        @Override
        protected final T defaultAction(Element e, Void ignore) {
            throw new IllegalArgumentException(e + " does not represent a " + label);
        }
    }
}
