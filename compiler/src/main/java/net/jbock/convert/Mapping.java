package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.convert.matching.MapExpr;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import java.util.Optional;

import static net.jbock.model.Multiplicity.OPTIONAL;

/**
 * An annotated method with additional information about type conversion.
 *
 * @param <M> the type of item
 */
public final class Mapping<M extends AnnotatedMethod> {

    private final MapExpr<M> mapExpr;
    private final Optional<CodeBlock> extractExpr;
    private final ParameterSpec asParameterSpec;
    private final FieldSpec asFieldSpec;

    private Mapping(
            MapExpr<M> mapExpr,
            Optional<CodeBlock> extractExpr,
            ParameterSpec asParameterSpec,
            FieldSpec asFieldSpec) {
        this.asParameterSpec = asParameterSpec;
        this.mapExpr = mapExpr;
        this.extractExpr = extractExpr;
        this.asFieldSpec = asFieldSpec;
    }

    public static <M extends AnnotatedMethod> Mapping<M> create(
            MapExpr<M> mapExpr,
            Optional<CodeBlock> extractExpr,
            SourceMethod<M> parameter) {
        TypeName fieldType = TypeName.get(parameter.returnType());
        String fieldName = parameter.enumName().original();
        FieldSpec asFieldSpec = FieldSpec.builder(fieldType, fieldName).build();
        ParameterSpec asParameterSpec = ParameterSpec.builder(fieldType, fieldName).build();
        return new Mapping<>(mapExpr, extractExpr, asParameterSpec, asFieldSpec);
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
        return mapExpr.match().multiplicity();
    }

    public MapExpr<M> mapExpr() {
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
        return mapExpr.modeFlag();
    }

    public SourceMethod<M> sourceMethod() {
        return mapExpr.sourceMethod();
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
