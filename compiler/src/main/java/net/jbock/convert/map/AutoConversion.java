package net.jbock.convert.map;

import com.squareup.javapoet.CodeBlock;

final class AutoConversion {

    private final String qualifiedName;
    private final CodeBlock code;
    private final boolean multiline;

    AutoConversion(
            String qualifiedName,
            CodeBlock code,
            boolean multiline) {
        this.qualifiedName = qualifiedName;
        this.code = code;
        this.multiline = multiline;
    }

    String qualifiedName() {
        return qualifiedName;
    }

    CodeBlock code() {
        return code;
    }

    boolean multiline() {
        return multiline;
    }
}
