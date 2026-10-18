package net.jbock.convert;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import net.jbock.annotated.Item;
import net.jbock.convert.match.Match;
import net.jbock.model.Arity;
import net.jbock.model.Multiplicity;

import java.util.Optional;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.common.Suppliers.memoize;
import static net.jbock.model.Multiplicity.OPTIONAL;

/**
 * An item with additional information about string conversion.
 */
public final class Mapping<M extends Item> {

    private final Optional<CodeBlock> createConverterExpression;
    private final Match<M> match;
    private final Arity arity;

    private Mapping(
            Optional<CodeBlock> createConverterExpression,
            Match<M> match,
            Arity arity) {
        this.createConverterExpression = createConverterExpression;
        this.match = match;
        this.arity = arity;
    }

    public static <M extends Item> Mapping<M> create(
            Match<M> match,
            Arity arity) {
        return new Mapping<>(Optional.empty(), match, arity);
    }

    public static <M extends Item> Mapping<M> create(
            CodeBlock createConverterExpression,
            Match<M> match) {
        return new Mapping<>(Optional.of(createConverterExpression), match, Arity.UNARY);
    }

    public static <M extends Item> Mapping<M> create(
            CodeBlock createConverterExpression,
            Match<M> match,
            Arity arity) {
        return new Mapping<>(Optional.of(createConverterExpression), match, arity);
    }

    public Optional<CodeBlock> createConverterExpression() {
        return createConverterExpression;
    }

    public Optional<CodeBlock> extractExpr() {
        return match.extractExpr();
    }

    public Multiplicity multiplicity() {
        return match.multiplicity();
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

    public boolean isNullary() {
        return arity == Arity.NULLARY;
    }

    public M item() {
        return match.item();
    }

    public String paramLabel() {
        return item().paramLabel();
    }

    private final Supplier<ParameterSpec> paramSupplier = memoize(() -> {
        TypeName fieldType = TypeName.get(item().returnType());
        String enumName = item().safeName();
        return ParameterSpec.builder(fieldType, enumName).build();
    });

    private final Supplier<FieldSpec> fieldSupplier = memoize(() -> {
        TypeName fieldType = TypeName.get(item().returnType());
        String fieldName = item().methodName();
        return FieldSpec.builder(fieldType, fieldName, FINAL).build();
    });

    public FieldSpec field() {
        return fieldSupplier.get();
    }

    public ParameterSpec param() {
        return paramSupplier.get();
    }
}
