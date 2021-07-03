package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import io.jbock.util.Optional;
import net.jbock.convert.Mapped;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;

import javax.lang.model.type.TypeMirror;

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

    public <P extends AbstractItem> Mapped<P> toConvertedParameter(
            MapExpr mapExpr, P parameter) {
        return Mapped.create(mapExpr, extractExpr, multiplicity, parameter);
    }

    public TypeMirror baseType() {
        return baseType;
    }

    public Multiplicity multiplicity() {
        return multiplicity;
    }
}
