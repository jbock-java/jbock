package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.common.Suppliers.memoize;

public sealed abstract class Item permits Parameter, Option {

    Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    private final Supplier<Either<Optional<ValidationFailure>, Converter>> converter = memoize(() -> {
        String annotation = converterAttribute();
        if (annotation.isEmpty()) {
            return Either.left(Optional.empty());
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(sourceElement().element().getEnclosedElements());
        for (ExecutableElement method : methods) {
            if (method.getSimpleName().contentEquals(annotation)) {
                if (method.getModifiers().contains(Modifier.PRIVATE)) {
                    return Either.left(Optional.of(new ValidationFailure("The converter must not be private", method)));
                }
                if (!method.getModifiers().contains(Modifier.STATIC)) {
                    return Either.left(Optional.of(new ValidationFailure("The converter must be static", method)));
                }
                return validateConverter(method).mapLeft(Optional::of);
            }
        }
        return Either.left(Optional.of(new ValidationFailure("Method not found: " + annotation, sourceElement().element())));
    });

    private Either<ValidationFailure, Converter> validateConverter(ExecutableElement converter) {
        // todo validate converter signature
        return Either.right(new Converter());
    }

    private final SourceElement sourceElement;

    Item(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    public final Either<Optional<ValidationFailure>, Converter> converter() {
        return converter.get();
    }

    final SourceElement sourceElement() {
        return sourceElement;
    }

    public abstract String paramLabel();

    public abstract String converterAttribute();

    public abstract Optional<String> descriptionKey();

    public abstract List<String> description();

    public abstract ExecutableElement method();

    public abstract String safeName();

    public final List<Modifier> accessModifiers() {
        return method().getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .toList();
    }

    public final ValidationFailure fail(String message) {
        return new ValidationFailure(message, method());
    }

    public final String methodName() {
        return method().getSimpleName().toString();
    }

    public final TypeMirror returnType() {
        return method().getReturnType();
    }
}
