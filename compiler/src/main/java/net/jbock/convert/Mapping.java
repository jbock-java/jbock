package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
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

    private final CodeBlock mapper;
    private final Match<M> match;
    private final boolean modeFlag;

    private Mapping(
            CodeBlock mapper,
            Match<M> match,
            boolean modeFlag) {
        this.mapper = mapper;
        this.match = match;
        this.modeFlag = modeFlag;
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
        CodeBlock mapper = CodeBlock.of("$T.create($T.identity())",
                StringConverter.class, Function.class);
        return new Mapping<>(mapper, match, true);
    }

    public CodeBlock mapper() {
        return mapper;
    }

    public Optional<CodeBlock> extractExpr() {
        return match.extractExpr();
    }

    public Multiplicity multiplicity() {
        return match.multiplicity();
    }

    public EnumName enumName() {
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

    public boolean isModeFlag() {
        return modeFlag;
    }

    public M sourceMethod() {
        return match.sourceMethod();
    }

    public String paramLabel() {
        return sourceMethod().paramLabel();
    }
}
