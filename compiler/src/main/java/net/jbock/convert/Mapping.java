package net.jbock.convert;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.FieldSpec;
import io.jbock.javapoet.TypeName;
import net.jbock.annotated.Item;
import net.jbock.convert.match.Match;
import net.jbock.model.Multiplicity;

import java.util.Optional;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.FINAL;
import static net.jbock.common.Suppliers.memoize;
import static net.jbock.model.Multiplicity.OPTIONAL;

/**
 * An annotated method with additional information about string conversion.
 *
 * @param <M> one of three types of annotated methods:
 *           named option, positional parameter, or repeatable positional parameter
 */
public final class Mapping<M extends Item> {

    private final CodeBlock createConverterExpression;
    private final Match<M> match;
    private final boolean nullary;

    private Mapping(
            CodeBlock createConverterExpression,
            Match<M> match,
            boolean nullary) {
        this.createConverterExpression = createConverterExpression;
        this.match = match;
        this.nullary = nullary;
    }

    public static <M extends Item>
    Mapping<M> create(
            CodeBlock createConverterExpression,
            Match<M> match) {
        return create(createConverterExpression, match, false);
    }

    public static <M extends Item>
    Mapping<M> create(
            CodeBlock createConverterExpression,
            Match<M> match,
            boolean nullary) {
        return new Mapping<>(createConverterExpression, match, nullary);
    }

    public CodeBlock createConverterExpression() {
        return createConverterExpression;
    }

    public Optional<CodeBlock> extractExpr() {
        return match.extractExpr();
    }

    public Multiplicity multiplicity() {
        return match.multiplicity();
    }

    public String enumName() {
        return sourceMethod().enumName();
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
        return nullary;
    }

    public M sourceMethod() {
        return match.sourceMethod();
    }

    public String paramLabel() {
        return sourceMethod().paramLabel();
    }

    private final Supplier<FieldSpec> fieldSupplier = memoize(() -> {
        TypeName fieldType = TypeName.get(sourceMethod().returnType());
        String fieldName = sourceMethod().methodName();
        return FieldSpec.builder(fieldType, fieldName, FINAL).build();
    });

    public FieldSpec field() {
        return fieldSupplier.get();
    }
}
