package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.type.TypeMirror;

public class MapExpr {

    private final CodeBlock code;
    private final TypeMirror type;
    private final boolean multiline;

    public MapExpr(CodeBlock code, TypeMirror type, boolean multiline) {
        this.code = code;
        this.type = type;
        this.multiline = multiline;
    }

    public CodeBlock code() {
        return code;
    }

    public TypeMirror type() {
        return type;
    }

    public boolean multiline() {
        return multiline;
    }
}
