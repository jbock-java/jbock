package net.jbock.convert;

import io.jbock.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.match.Match;
import net.jbock.model.Multiplicity;
import net.jbock.util.StringConverter;

import java.util.Optional;
import java.util.function.Function;

import static net.jbock.model.Multiplicity.OPTIONAL;

/**
 * An annotated method with additional information about string conversion.
 *
 * @param <M> one of three types of annotated methods:
 *           named option, positional parameter, or repeatable positional parameter
 */
public final class Mapping<M extends AnnotatedMethod> {

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

    public static <M extends AnnotatedMethod>
    Mapping<M> create(
            CodeBlock mapper,
            Match<M> match) {
        return new Mapping<>(mapper, match, false);
    }

    public static <M extends AnnotatedMethod>
    Mapping<M> createModeFlag(
            Match<M> match) {
        CodeBlock createConverterExpression = CodeBlock.of("$T.create($T.identity())",
                StringConverter.class, Function.class);
        return new Mapping<>(createConverterExpression, match, true);
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
}
