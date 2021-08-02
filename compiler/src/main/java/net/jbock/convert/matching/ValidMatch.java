package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.model.Multiplicity;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

public final class ValidMatch<M extends AnnotatedMethod> {

    private final Match<M> match;

    ValidMatch(Match<M> match) {
        this.match = match;
    }

    public TypeMirror baseType() {
        return match.baseType();
    }

    public Multiplicity multiplicity() {
        return match.multiplicity();
    }

    public M sourceMethod() {
        return match.sourceMethod();
    }

    public Optional<CodeBlock> extractExpr() {
        return match.extractExpr();
    }
}
