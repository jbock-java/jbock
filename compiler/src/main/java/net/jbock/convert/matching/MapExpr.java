package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.source.SourceMethod;
import net.jbock.util.StringConverter;

import javax.lang.model.type.TypeMirror;
import java.util.function.Function;

public class MapExpr<M extends AnnotatedMethod> {

    private final CodeBlock code;
    private final Match<M> match;
    private final boolean multiline;
    private final boolean modeFlag;

    public MapExpr(
            CodeBlock code,
            Match<M> match,
            boolean multiline,
            boolean modeFlag) {
        this.code = code;
        this.match = match;
        this.multiline = multiline;
        this.modeFlag = modeFlag;
    }

    public static <M extends AnnotatedMethod> Mapping<M> create(
            CodeBlock code,
            Match<M> match,
            boolean multiline) {
        return new MapExpr<>(code, match, multiline, false).toMapping();
    }

    public static Mapping<AnnotatedOption> createFlag(Match<AnnotatedOption> match) {
        CodeBlock code = CodeBlock.of("$T.create($T.identity())", StringConverter.class, Function.class);
        return new MapExpr<>(code, match, false, true).toMapping();
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

    public Match<M> match() {
        return match;
    }

    public boolean modeFlag() {
        return modeFlag;
    }

    private Mapping<M> toMapping() {
        return Mapping.create(this, match.extractExpr(), match.sourceMethod());
    }
}
