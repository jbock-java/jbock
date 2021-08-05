package net.jbock.processor;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.testing.compile.JavaFileObjects.forSourceLines;

class Processor {

    static JbockProcessor testInstance() {
        return new JbockProcessor();
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
                "import net.jbock.Parameter;",
                "import net.jbock.Parameters;",
                "import net.jbock.Option;",
                "");
        List<String> moreLines = new ArrayList<>(lines.length + header.size());
        moreLines.addAll(header);
        Collections.addAll(moreLines, lines);
        return moreLines;
    }
}
