package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.Mapping;
import net.jbock.source.SourceMethod;

import javax.lang.model.type.TypeMirror;

public class MapExpr<M extends AnnotatedMethod> {

    private final CodeBlock code;
    private final Match<M> match;
    private final boolean multiline;

    public MapExpr(CodeBlock code, Match<M> match, boolean multiline) {
        this.code = code;
        this.match = match;
        this.multiline = multiline;
    }

    public CodeBlock code() {
        return code;
    }

    public TypeMirror type() {
        return match.baseType();
    }

    public boolean multiline() {
        return multiline;
    }

    public SourceMethod<M> sourceMethod() {
        return match.sourceMethod();
    }

    public Mapping<M> toMapping() {
        return Mapping.create(this, match.extractExpr(), match.multiplicity(), match.sourceMethod());
    }
}
