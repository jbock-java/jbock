package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.convert.matching.ValidMatch;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.OPTIONAL;

/**
 * An annotated method with additional information about type conversion.
 *
 * @param <M> the type of item
 */
public final class Mapping<M extends AnnotatedMethod> {

    private final CodeBlock mapExpr;
    private final ValidMatch<M> match;
    private final boolean multiline;
    private final boolean modeFlag;
    private final ParameterSpec asParameterSpec;
    private final FieldSpec asFieldSpec;

    private Mapping(
            CodeBlock mapExpr,
            ValidMatch<M> match,
            boolean multiline,
            boolean modeFlag,
            ParameterSpec asParameterSpec,
            FieldSpec asFieldSpec) {
        this.asParameterSpec = asParameterSpec;
        this.match = match;
        this.multiline = multiline;
        this.modeFlag = modeFlag;
        this.mapExpr = mapExpr;
        this.asFieldSpec = asFieldSpec;
    }

    public static <M extends AnnotatedMethod> Mapping<M> create(
            CodeBlock code,
            ValidMatch<M> match,
            boolean multiline) {
        return Mapping.create(code, match, multiline, false);
    }

    public static <M extends AnnotatedMethod> Mapping<M> createFlag(
            CodeBlock code,
            ValidMatch<M> match) {
        return Mapping.create(code, match, false, true);
    }

    private static <M extends AnnotatedMethod> Mapping<M> create(
            CodeBlock mapExpr,
            ValidMatch<M> match,
            boolean multiline,
            boolean modeFlag) {
        TypeName fieldType = TypeName.get(match.sourceMethod().returnType());
        String fieldName = match.sourceMethod().enumName().original();
        FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
        ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
        return new Mapping<>(mapExpr, match, multiline, modeFlag, asParameterSpec, asFieldSpec);
    }

    public Optional<CodeBlock> simpleMapExpr() {
        if (multiline) {
            return Optional.empty();
        }
        return Optional.of(mapExpr);
    }

    public Optional<CodeBlock> extractExpr() {
        return match.extractExpr();
    }

    public Multiplicity multiplicity() {
        return match.multiplicity();
    }

    public boolean multiline() {
        return multiline;
    }

    public TypeMirror baseType() {
        return match.baseType();
    }

    public CodeBlock mapExpr() {
        return mapExpr;
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

    public boolean isFlag() {
        return modeFlag;
    }

    public SourceMethod<M> sourceMethod() {
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
