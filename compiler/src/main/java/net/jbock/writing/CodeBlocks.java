package net.jbock.writing;

import com.palantir.javapoet.CodeBlock;

import java.util.List;

final class CodeBlocks {

    static CodeBlock joinByNewline(List<CodeBlock> code) {
        return CodeBlock.join(code, System.lineSeparator());
    }

    private CodeBlocks() {
    }
}
