package net.jbock.annotated;

import javax.inject.Inject;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.Parameters;
import net.jbock.VarargsParameter;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind.WARNING;
import static net.jbock.common.TypeTool.ANNOTATION_VALUE_AS_TYPE;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

abstract class Executable {

    private static final Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    private final ExecutableElement method;
    private final Optional<TypeElement> converter;

    Executable(ExecutableElement method, Optional<TypeElement> converter) {
        this.method = method;
        this.converter = converter;
    }

    static class Factory {
        private final Messager messager;

        @Inject
        Factory(Messager messager) {
            this.messager = messager;
        }

        Executable create(
                ExecutableElement method,
                Annotation annotation) {
            String canonicalName = annotation.annotationType().getCanonicalName();
            AnnotationMirror annotationMirror = method.getAnnotationMirrors().stream()
                    .filter(mirror -> AS_TYPE_ELEMENT.visit(mirror.getAnnotationType().asElement())
                            .map(TypeElement::getQualifiedName)
                            .map(Name::toString)
                            .filter(canonicalName::equals)
                            .isPresent())
                    .findFirst()
                    .orElseThrow(AssertionError::new);
            Optional<TypeElement> converter = findConverterAttribute(annotationMirror);
            if (annotation instanceof Option) {
                return new ExecutableOption(method, (Option) annotation, converter);
            }
            if (annotation instanceof Parameter) {
                return new ExecutableParameter(method, (Parameter) annotation, converter);
            }
            if (annotation instanceof VarargsParameter) {
                return new ExecutableVarargsParameter(method, (VarargsParameter) annotation, converter);
            }
            if (annotation instanceof Parameters) {
                messager.printMessage(WARNING,
                        "@Parameters has been deprecated, use @VarargsParameter instead", method);
                return new ExecutableVarargsParameter(method, convertLegacyParameters((Parameters) annotation), converter);
            }
            throw new AssertionError();
        }
    }

    private static VarargsParameter convertLegacyParameters(Parameters parameters) {
        return new VarargsParameter() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return parameters.annotationType();
            }

            @Override
            public Class<?> converter() {
                return parameters.converter();
            }

            @Override
            public String descriptionKey() {
                return parameters.descriptionKey();
            }

            @Override
            public String[] description() {
                return parameters.description();
            }

            @Override
            public String paramLabel() {
                return parameters.paramLabel();
            }
        };
    }

    abstract AnnotatedMethod annotatedMethod(SourceElement sourceElement, String enumName);

    abstract Optional<String> descriptionKey();

    abstract List<String> description();

    final ExecutableElement method() {
        return method;
    }

    final Name simpleName() {
        return method.getSimpleName();
    }

    final List<Modifier> accessModifiers() {
        return method().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(toList());
    }

    private static Optional<TypeElement> findConverterAttribute(AnnotationMirror annotationMirror) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                annotationMirror.getElementValues();
        return elementValues.entrySet().stream()
                .filter(e -> "converter".contentEquals(e.getKey().getSimpleName()))
                .map(Map.Entry::getValue)
                .findFirst()
                .flatMap(ANNOTATION_VALUE_AS_TYPE::visit)
                .flatMap(AS_DECLARED::visit)
                .map(DeclaredType::asElement)
                .flatMap(AS_TYPE_ELEMENT::visit)
                .filter(element -> !"java.lang.Void".contentEquals(element.getQualifiedName()));
    }

    final Optional<TypeElement> converter() {
        return converter;
    }

    final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method);
    }
}
