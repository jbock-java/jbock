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
    private final M item;

    private Match(
            TypeMirror baseType,
            Multiplicity multiplicity,
            Optional<CodeBlock> extractExpr,
            M item) {
        this.baseType = baseType;
        this.multiplicity = multiplicity;
        this.extractExpr = extractExpr;
        this.item = item;
    }

    static <M extends Item>
    Match<M> createWithExtract(
            TypeMirror baseType,
            CodeBlock extractExpr,
            M item) {
        return new Match<>(baseType, OPTIONAL, Optional.of(extractExpr), item);
    }

    static <M extends Item>
    Match<M> create(
            TypeMirror baseType,
            Multiplicity multiplicity,
            M item) {
        return new Match<>(baseType, multiplicity, Optional.empty(), item);
    }

    public TypeMirror baseType() {
        return baseType;
    }

    public Multiplicity multiplicity() {
        return multiplicity;
    }

    public M item() {
        return item;
    }

    public ValidationFailure fail(String message) {
        return item().fail(message);
    }

    public Optional<CodeBlock> extractExpr() {
        return extractExpr;
    }
}
