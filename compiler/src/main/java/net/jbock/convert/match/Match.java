package net.jbock.convert.match;

import io.jbock.javapoet.CodeBlock;
import net.jbock.annotated.Item;
import net.jbock.common.ValidationFailure;
import net.jbock.model.Multiplicity;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.OPTIONAL;

public final class Match<M extends Item> {

    /* baseType (List<A>) == A
    /* baseType (Optional<A>) == A
     * baseType (OptionalInt) == Integer
     * baseType (int) == Integer
     */
    private final TypeMirror baseType;
    private final Optional<CodeBlock> extractExpr;
    private final Multiplicity multiplicity;
    private final M sourceMethod;

    private Match(
            TypeMirror baseType,
            Multiplicity multiplicity,
            Optional<CodeBlock> extractExpr,
            M sourceMethod) {
        this.baseType = baseType;
        this.multiplicity = multiplicity;
        this.extractExpr = extractExpr;
        this.sourceMethod = sourceMethod;
    }

    static <M extends Item>
    Match<M> createWithExtract(
            TypeMirror baseType,
            CodeBlock extractExpr,
            M sourceMethod) {
        return new Match<>(baseType, OPTIONAL, Optional.of(extractExpr), sourceMethod);
    }

    static <M extends Item>
    Match<M> create(
            TypeMirror baseType,
            Multiplicity multiplicity,
            M sourceMethod) {
        return new Match<>(baseType, multiplicity, Optional.empty(), sourceMethod);
    }

    public TypeMirror baseType() {
        return baseType;
    }

    public Multiplicity multiplicity() {
        return multiplicity;
    }

    public M sourceMethod() {
        return sourceMethod;
    }

    public ValidationFailure fail(String message) {
        return sourceMethod().fail(message);
    }

    public Optional<CodeBlock> extractExpr() {
        return extractExpr;
    }
}
