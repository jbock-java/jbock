package net.jbock.annotated;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoMappings;
import net.jbock.convert.match.OptionalPrimitive;
import net.jbock.model.Multiplicity;
import net.jbock.processor.SourceElement;
import net.jbock.util.Either;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
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

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.common.Suppliers.memoize;
import static net.jbock.common.TypeTool.AS_PRIMITIVE;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.model.Multiplicity.OPTIONAL;

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

    private final Supplier<Either<ValidationFailure, CodeBlock>> converter = memoize(() -> {
        String annotation = converterAttribute();
        if (annotation.isEmpty()) {
            return Either.left(new ValidationFailure("Converter not found", method()));
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(sourceElement().element().getEnclosedElements());
        for (ExecutableElement method : methods) {
            if (method.getSimpleName().contentEquals(annotation)) {
                if (method.getModifiers().contains(Modifier.PRIVATE)) {
                    return Either.left(new ValidationFailure("The converter must not be private", method));
                }
                if (!method.getModifiers().contains(Modifier.STATIC)) {
                    return Either.left(new ValidationFailure("The converter must be static", method));
                }
                return validateConverterMethod(method);
            }
        }
        return Either.left(new ValidationFailure("Method not found: " + annotation, sourceElement().element()));
    });

    private final Supplier<Either<ValidationFailure, Optional<CodeBlock>>> createConverterExpression = memoize(() -> {
        String converterAttribute = converterAttribute();
        if (!converterAttribute.isEmpty()) {
            return converter().map(Optional::of);
        }
        if (isEnum()) {
            return Either.right(Optional.of(AutoMappings.findEnumMapping(baseType())));
        }
        if (isString()) {
            return Either.right(Optional.empty());
        }
        if (isBool()) {
            return Either.right(Optional.empty());
        }
        Optional<CodeBlock> result = AutoMappings.findAutoMapping(tool(), baseType());
        if (result.isEmpty()) {
            return Either.left(fail("unknown type: " + Util.typeToString(baseType())));
        } else {
            return Either.right(result);
        }
    });

    private Either<ValidationFailure, CodeBlock> validateConverterMethod(ExecutableElement method) {
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
        String standardMsg = String.format("The return type must be Either<String, %s>", Util.typeToString(baseType()));
        if (!tool().isErasureOf(method.getReturnType(), Either.class)) {
            return Either.left(new ValidationFailure(standardMsg, method));
        }
        Optional<DeclaredType> visit = TypeTool.AS_DECLARED.visit(method.getReturnType());
        if (visit.isEmpty()) {
            return Either.left(new ValidationFailure("Unexpected: Either type not found", method));
        }
        DeclaredType declaredType = visit.orElseThrow();
        if (declaredType.getTypeArguments().size() != 2) {
            return Either.left(new ValidationFailure(standardMsg, method));
        }
        if (!tool().isString(declaredType.getTypeArguments().getFirst())) {
            return Either.left(new ValidationFailure(standardMsg, method));
        }
        if (!tool().types().isSameType(declaredType.getTypeArguments().get(1), baseType())) {
            return Either.left(new ValidationFailure(standardMsg, method));
        }
        return Either.right(CodeBlock.of("$T::$L", sourceElement().typeName(), method.getSimpleName()));
    }

    private final Supplier<ParameterSpec> paramSupplier = memoize(() -> {
        TypeName fieldType = TypeName.get(returnType());
        String enumName = safeName();
        return ParameterSpec.builder(fieldType, enumName).build();
    });

    private final Supplier<FieldSpec> fieldSupplier = memoize(() -> {
        TypeName fieldType = TypeName.get(returnType());
        String fieldName = methodName();
        return FieldSpec.builder(fieldType, fieldName, FINAL).build();
    });

    private final SourceElement sourceElement;

    Item(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    public final Either<ValidationFailure, CodeBlock> converter() {
        return converter.get();
    }

    public final Either<ValidationFailure, Optional<CodeBlock>> createConverterExpressionEither() {
        return createConverterExpression.get();
    }

    public final Optional<ValidationFailure> validateConverterExpression() {
        return createConverterExpression.get().getLeft();
    }

    public Optional<CodeBlock> createConverterExpression() {
        if (createConverterExpressionEither().isLeft()) {
            throw new IllegalStateException("boo");
        }
        Optional<Optional<CodeBlock>> result = createConverterExpressionEither().getRight();
        if (result.isEmpty()) {
            return Optional.empty();
        }
        return result.orElseThrow();
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
        return tool().isErasureOf(returnType(), List.class);
    }

    public final boolean isString() {
        return tool().isSameType(baseType(), String.class);
    }

    public final boolean isEnum() {
        Optional<DeclaredType> visit = TypeTool.AS_DECLARED.visit(baseType());
        if (visit.isEmpty()) {
            return false;
        }
        Optional<TypeElement> tel = AS_TYPE_ELEMENT.visit(visit.orElseThrow().asElement());
        if (tel.isEmpty()) {
            return false;
        }
        return tel.orElseThrow().getKind() == ElementKind.ENUM;
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

    public final Multiplicity multiplicity() {
        if (tool().isSameType(returnType(), OptionalInt.class)) {
            return OPTIONAL;
        }
        if (tool().isSameType(returnType(), OptionalLong.class)) {
            return OPTIONAL;
        }
        if (tool().isSameType(returnType(), OptionalDouble.class)) {
            return OPTIONAL;
        }
        if (tool().isErasureOf(returnType(), Optional.class)) {
            return OPTIONAL;
        }
        if (isBool()) {
            return Multiplicity.OPTIONAL;
        }
        if (isList()) {
            return Multiplicity.REPEATABLE;
        }
        return Multiplicity.REQUIRED;
    }

    abstract boolean isBool();

    public FieldSpec field() {
        return fieldSupplier.get();
    }

    public ParameterSpec param() {
        return paramSupplier.get();
    }

    public boolean isRequired() {
        return multiplicity() == Multiplicity.REQUIRED;
    }

    public boolean isRepeatable() {
        return multiplicity() == Multiplicity.REPEATABLE;
    }

    public boolean isOptional() {
        return multiplicity() == OPTIONAL;
    }
}
