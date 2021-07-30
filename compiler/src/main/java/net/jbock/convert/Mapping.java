package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;
import net.jbock.convert.matching.MapExpr;
import net.jbock.convert.matching.Match;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;
import net.jbock.util.StringConverter;

import javax.lang.model.type.PrimitiveType;
import java.util.Optional;
import java.util.function.Function;

/**
 * An annotated method with additional information about type conversion.
 *
 * @param <M> the type of item
 */
public final class Mapping<M extends AnnotatedMethod> {

    private final MapExpr mapExpr;
    private final Optional<CodeBlock> extractExpr;
    private final Multiplicity multiplicity;
    private final SourceMethod<M> sourceMethod;
    private final ParameterSpec asParameterSpec;
    private final FieldSpec asFieldSpec;
    private final boolean modeFlag;

    private Mapping(
            MapExpr mapExpr,
            Optional<CodeBlock> extractExpr,
            Multiplicity multiplicity,
            ParameterSpec asParameterSpec,
            FieldSpec asFieldSpec,
            SourceMethod<M> sourceMethod,
            boolean modeFlag) {
        this.asParameterSpec = asParameterSpec;
        this.mapExpr = mapExpr;
        this.extractExpr = extractExpr;
        this.multiplicity = multiplicity;
        this.asFieldSpec = asFieldSpec;
        this.sourceMethod = sourceMethod;
        this.modeFlag = modeFlag;
    }

    public static <M extends AnnotatedMethod> Mapping<M> create(
            MapExpr mapExpr,
            Optional<CodeBlock> extractExpr,
            Multiplicity multiplicity,
            SourceMethod<M> parameter) {
        TypeName fieldType = TypeName.get(parameter.returnType());
        String fieldName = parameter.enumName().original();
        FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
        ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
        return new Mapping<>(mapExpr, extractExpr, multiplicity, asParameterSpec,
                asFieldSpec, parameter, false);
    }

    public static Mapping<AnnotatedOption> createFlag(
            SourceMethod<AnnotatedOption> namedOption,
            PrimitiveType booleanType) {
        CodeBlock code = CodeBlock.of("$T.create($T.identity())", StringConverter.class, Function.class);
        TypeName fieldType = TypeName.BOOLEAN;
        String fieldName = namedOption.enumName().original();
        FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
        ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
        Match match = Match.create(booleanType, Multiplicity.OPTIONAL);
        MapExpr mapExpr = new MapExpr(code, match, false);
        return new Mapping<>(mapExpr, Optional.empty(), Multiplicity.OPTIONAL, asParameterSpec,
                asFieldSpec, namedOption, true);
    }

    public Optional<CodeBlock> simpleMapExpr() {
        if (mapExpr.multiline()) {
            return Optional.empty();
        }
        return Optional.of(mapExpr.code());
    }

    public Optional<CodeBlock> extractExpr() {
        return extractExpr;
    }

    public Multiplicity multiplicity() {
        return multiplicity;
    }

    public MapExpr mapExpr() {
        return mapExpr;
    }

    public EnumName enumName() {
        return sourceMethod.enumName();
    }

    public boolean isRequired() {
        return multiplicity == Multiplicity.REQUIRED;
    }

    public boolean isRepeatable() {
        return multiplicity == Multiplicity.REPEATABLE;
    }

    public boolean isOptional() {
        return multiplicity == Multiplicity.OPTIONAL;
    }

    public boolean isFlag() {
        return modeFlag;
    }

    public SourceMethod<M> item() {
        return sourceMethod;
    }

    public String paramLabel() {
        return sourceMethod.paramLabel();
    }

    public FieldSpec asField() {
        return asFieldSpec;
    }

    public ParameterSpec asParam() {
        return asParameterSpec;
    }
}
