package net.jbock.annotated;

import com.palantir.javapoet.CodeBlock;
import io.jbock.util.Either;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.match.OptionalPrimitive;
import net.jbock.model.Arity;
import net.jbock.model.Multiplicity;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.common.Suppliers.memoize;
import static net.jbock.common.TypeTool.AS_PRIMITIVE;

public sealed abstract class Item permits Parameter, Option {

    Set<Modifier> ACCESS_MODIFIERS = EnumSet.of(PUBLIC, PROTECTED);

    private final Supplier<TypeMirror> baseType = memoize(() -> {
        if (isList()) {
            Optional<TypeMirror> t = tool().getSingleTypeArgument(returnType(), List.class);
            if (t.isPresent()) {
                return t.orElseThrow();
            }
        }
        if (isOptional()) {
            Optional<TypeMirror> t = tool().getSingleTypeArgument(returnType(), Optional.class);
            if (t.isPresent()) {
                return t.orElseThrow();
            }
        }
        if (tool().isSameType(returnType(), OptionalInt.class)) {
            Optional<TypeMirror> t = tool().asType(Integer.class);
            if (t.isPresent()) {
                return t.orElseThrow();
            }
        }
        if (tool().isSameType(returnType(), OptionalLong.class)) {
            Optional<TypeMirror> t = tool().asType(Long.class);
            if (t.isPresent()) {
                return t.orElseThrow();
            }
        }
        if (tool().isSameType(returnType(), OptionalDouble.class)) {
            Optional<TypeMirror> t = tool().asType(Double.class);
            if (t.isPresent()) {
                return t.orElseThrow();
            }
        }
        if (returnType().getKind().isPrimitive()) {
            Optional<PrimitiveType> t = AS_PRIMITIVE.visit(returnType());
            if (t.isPresent()) {
                return tool().types().boxedClass(t.orElseThrow()).asType();
            }
        }
        return returnType();
    });

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

    private Either<ValidationFailure, Converter> validateConverter(ExecutableElement method) {
        // parameter validation
        if (method.getParameters().isEmpty()) {
            return Either.left(new ValidationFailure("parameter expected", method));
        }
        if (method.getParameters().size() >= 2) {
            return Either.left(new ValidationFailure("single parameter expected", method));
        }
        if (!tool().isString(method.getParameters().getFirst().asType())) {
            return Either.left(new ValidationFailure("The parameter type must be String", method));
        }
        method.getReturnType();
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

    final TypeTool tool() {
        return sourceElement.tool();
    }

    final SafeTypes types() {
        return tool().types();
    }

    final SafeElements elements() {
        return tool().elements();
    }

    public abstract String paramLabel();

    public abstract String converterAttribute();

    public abstract Optional<String> descriptionKey();

    public abstract List<String> description();

    public abstract ExecutableElement method();

    public abstract String safeName();

    public final List<Modifier> accessModifiers() {
        return method().getModifiers().stream().filter(ACCESS_MODIFIERS::contains).toList();
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

    /* baseType (List<A>) == A
    /* baseType (Optional<A>) == A
     * baseType (OptionalInt) == Integer
     * baseType (int) == Integer
     */
    public final TypeMirror baseType() {
        return baseType.get();
    }

    public final boolean isList() {
        return tool().isErasureOf(tool().types().erasure(returnType()), List.class);
    }

    public final boolean isOptional() {
        if (tool().isSameType(returnType(), OptionalInt.class)) {
            return true;
        }
        if (tool().isSameType(returnType(), OptionalLong.class)) {
            return true;
        }
        if (tool().isSameType(returnType(), OptionalDouble.class)) {
            return true;
        }
        return tool().isErasureOf(tool().types().erasure(returnType()), Optional.class);
    }

    public Optional<CodeBlock> extractExpr() {
        if (tool().isSameType(returnType(), OptionalInt.class)) {
            return Optional.of(OptionalPrimitive.INT.extractExpr());
        }
        if (tool().isSameType(returnType(), OptionalLong.class)) {
            return Optional.of(OptionalPrimitive.LONG.extractExpr());
        }
        if (tool().isSameType(returnType(), OptionalDouble.class)) {
            return Optional.of(OptionalPrimitive.DOUBLE.extractExpr());
        }
        return Optional.empty();
    }

    // todo implement
    public abstract Multiplicity multiplicity();

    // todo implement
    public abstract Arity arity();

    public final boolean isBool() {
        return returnType().getKind() == TypeKind.BOOLEAN;
    }
}
