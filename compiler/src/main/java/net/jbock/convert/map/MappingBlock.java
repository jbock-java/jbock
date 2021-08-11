package net.jbock.convert.map;

import com.squareup.javapoet.CodeBlock;

public final class MappingBlock {

    private final CodeBlock code;
    private final boolean multiline;

    public MappingBlock(
            CodeBlock code,
            boolean multiline) {
        this.code = code;
        this.multiline = multiline;
    }

    public CodeBlock code() {
        return code;
    }

    public boolean multiline() {
        return multiline;
    }
}
