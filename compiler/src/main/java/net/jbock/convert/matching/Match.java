package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public class Match<M extends AnnotatedMethod> {

    /* baseType ({List<A>, Optional<A>}) == A
     * baseType (OptionalInt) == Integer
     * baseType (int) == Integer
     */
    private final TypeMirror baseType;
    private final Optional<CodeBlock> extractExpr;
    private final Multiplicity multiplicity;
    private final SourceMethod<M> sourceMethod;

    private Match(
            TypeMirror baseType,
            Multiplicity multiplicity,
            Optional<CodeBlock> extractExpr,
            SourceMethod<M> sourceMethod) {
        this.baseType = baseType;
        this.multiplicity = multiplicity;
        this.extractExpr = extractExpr;
        this.sourceMethod = sourceMethod;
    }

    public static <M extends AnnotatedMethod> Match<M> create(
            TypeMirror baseType,
            Multiplicity multiplicity,
            CodeBlock extractExpr,
            SourceMethod<M> sourceMethod) {
        return new Match<>(baseType, multiplicity, Optional.of(extractExpr), sourceMethod);
    }

    public static <M extends AnnotatedMethod> Match<M> create(
            TypeMirror baseType,
            Multiplicity multiplicity,
            SourceMethod<M> sourceMethod) {
        return new Match<>(baseType, multiplicity, Optional.empty(), sourceMethod);
    }

    public TypeMirror baseType() {
        return baseType;
    }

    public Multiplicity multiplicity() {
        return multiplicity;
    }

    public SourceMethod<M> sourceMethod() {
        return sourceMethod;
    }

    public Optional<CodeBlock> extractExpr() {
        return extractExpr;
    }

}
