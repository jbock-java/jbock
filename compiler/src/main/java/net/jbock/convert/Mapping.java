package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.convert.match.Match;
import net.jbock.model.Multiplicity;
import net.jbock.util.StringConverter;

import java.util.Optional;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.model.Multiplicity.OPTIONAL;

/**
 * An annotated method with additional information about type conversion.
 *
 * @param <M> one of three types of annotated methods:
 *           named option, positional parameter, or repeatable positional parameter
 */
public final class Mapping<M extends AnnotatedMethod> {

    private final CodeBlock mapper;
    private final Match<M> match;
    private final boolean modeFlag;
    private final ParameterSpec asParameterSpec;
    private final FieldSpec asFieldSpec;

    private Mapping(
            CodeBlock mapper,
            Match<M> match,
            boolean modeFlag,
            ParameterSpec asParameterSpec,
            FieldSpec asFieldSpec) {
        this.mapper = mapper;
        this.asParameterSpec = asParameterSpec;
        this.match = match;
        this.modeFlag = modeFlag;
        this.asFieldSpec = asFieldSpec;
    }

    public static <M extends AnnotatedMethod>
    Mapping<M> create(
            CodeBlock mapper,
            Match<M> match) {
        return create(mapper, match, false);
    }

    public static <M extends AnnotatedMethod>
    Mapping<M> createFlag(
            Match<M> match) {
        CodeBlock mapper = CodeBlock.of("$T.create($T.identity())",
                StringConverter.class, Function.class);
        return create(mapper, match, true);
    }

    private static <M extends AnnotatedMethod>
    Mapping<M> create(
            CodeBlock mapper,
            Match<M> match,
            boolean modeFlag) {
        TypeName fieldType = TypeName.get(match.sourceMethod().returnType());
        String fieldName = match.sourceMethod().methodName();
        FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName)
                .addModifiers(PRIVATE, FINAL).build();
        ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, '_' + fieldName).build();
        return new Mapping<>(mapper, match, modeFlag, asParameterSpec, asFieldSpec);
    }

    public CodeBlock mapExpr() {
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

    public boolean modeFlag() {
        return modeFlag;
    }

    public M sourceMethod() {
        return match.sourceMethod();
    }

    public String paramLabel() {
        return sourceMethod().paramLabel();
    }

    public FieldSpec asField() {
        return asFieldSpec;
    }

    public ParameterSpec asParam() {
        return asParameterSpec;
    }
}
