package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.Mapped;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class Match {

    /* baseType ({List<A>, Optional<A>}) == A
     * baseType (OptionalInt) == Integer
     * baseType (int) == Integer
     */
    private final TypeMirror baseType;
    private final Optional<CodeBlock> extractExpr;
    private final Multiplicity multiplicity;

    private Match(
            TypeMirror baseType,
            Multiplicity multiplicity,
            Optional<CodeBlock> extractExpr) {
        this.baseType = baseType;
        this.multiplicity = multiplicity;
        this.extractExpr = extractExpr;
    }

    public static Match create(
            TypeMirror baseType,
            Multiplicity multiplicity,
            CodeBlock extractExpr) {
        return new Match(baseType, multiplicity, Optional.of(extractExpr));
    }

    public static Match create(
            TypeMirror baseType,
            Multiplicity multiplicity) {
        return new Match(baseType, multiplicity, Optional.empty());
    }

    public <M extends AnnotatedMethod> Mapped<M> toConvertedParameter(
            MapExpr mapExpr, SourceMethod<M> parameter) {
        return Mapped.create(mapExpr, extractExpr, multiplicity, parameter);
    }

    public TypeMirror baseType() {
        return baseType;
    }

    public Multiplicity multiplicity() {
        return multiplicity;
    }
}
