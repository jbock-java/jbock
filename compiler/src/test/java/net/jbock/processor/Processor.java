package net.jbock.processor;

import io.jbock.testing.compile.Compiler;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.jbock.testing.compile.Compiler.javac;
import static io.jbock.testing.compile.JavaFileObjects.forSourceLines;

class Processor {

    static JbockProcessor testInstance() {
        return new JbockProcessor();
    }

    static Compiler compiler() {
        return javac().withProcessors(List.of(new JbockProcessor()));
    }

    static Compiler compiler(javax.annotation.processing.Processor... additionalProcessors) {
        List<javax.annotation.processing.Processor> processors = new ArrayList<>();
        processors.add(new JbockProcessor());
        Collections.addAll(processors, additionalProcessors);
        return javac().withProcessors(processors);
    }

    static JavaFileObject fromSource(String... lines) {
        List<String> sourceLines = withImports(lines);
        return forSourceLines("test.Arguments", sourceLines);
    }

    private static List<String> withImports(String... lines) {
        List<String> header = List.of(
                "package test;",
                "",
                "import java.util.List;",
                "import java.util.Optional;",
                "import java.util.function.Supplier;",
                "",
                "import net.jbock.util.StringConverter;",
                "import net.jbock.Command;",
                "import net.jbock.SuperCommand;",
                "import net.jbock.Parameter;",
                "import net.jbock.VarargsParameter;",
                "import net.jbock.Option;",
                "");
        List<String> moreLines = new ArrayList<>(lines.length + header.size());
        moreLines.addAll(header);
        Collections.addAll(moreLines, lines);
        return moreLines;
    }
}
