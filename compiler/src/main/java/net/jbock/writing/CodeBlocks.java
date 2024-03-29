package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;

import java.util.List;

final class CodeBlocks {

    static CodeBlock joinByNewline(List<CodeBlock> code) {
        boolean indent = false;
        CodeBlock.Builder result = CodeBlock.builder();
        for (int i = 0; i < code.size(); i++) {
            if (i == 0) {
                result.add(code.get(i));
            } else if (i == 1) {
                result.add("\n").indent().add(code.get(i));
                indent = true;
            } else {
                result.add("\n").add(code.get(i));
            }
        }
        if (indent) {
            result.unindent();
        }
        return result.build();
    }

    static CodeBlock joinByComma(List<CodeBlock> code) {
        CodeBlock.Builder args = CodeBlock.builder();
        for (int i = 0; i < code.size(); i++) {
            if (i != 0) {
                args.add(",$W");
            }
            args.add(code.get(i));
        }
        return args.build();
    }

    private CodeBlocks() {
    }
}
