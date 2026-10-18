package net.jbock.writing;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.MethodSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.convert.Mapping;
import net.jbock.model.CommandModel;
import net.jbock.model.Multiplicity;
import net.jbock.model.Parameter;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.palantir.javapoet.MethodSpec.methodBuilder;
import static net.jbock.common.Suppliers.memoize;

@Inject
final class CreateModelMethod {
    private final CommandRepresentation cr;

    CreateModelMethod(CommandRepresentation cr) {
        this.cr = cr;
    }

    private final Supplier<MethodSpec> define = memoize(() -> {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("return $T.builder()", CommandModel.class));
        cr().sourceElement().descriptionKey().ifPresent(key ->
                code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String descriptionLine : cr().sourceElement().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", descriptionLine));
        }
        code.add(CodeBlock.of(".withProgramName($S)", cr().sourceElement().programName()));
        if (cr().isSuperCommand()) {
            code.add(CodeBlock.of(".withSuperCommand($L)", true));
        }
        for (Mapping<Option> c : cr().namedOptions()) {
            code.add(optionBlock(c));
        }
        for (Mapping<net.jbock.annotated.Parameter> c : cr().positionalParameters()) {
            code.add(parameterBlock(c));
        }
        code.add(CodeBlock.of(".build()"));
        return methodBuilder("createModel")
                .addStatement(CodeBlock.join(code, System.lineSeparator()))
                .returns(CommandModel.class)
                .addModifiers(cr().sourceElement().accessModifiers())
                .addModifiers(Modifier.STATIC)
                .build();
    });

    MethodSpec get() {
        return define.get();
    }

    private CodeBlock optionBlock(Mapping<Option> m) {
        List<CodeBlock> names = new ArrayList<>();
        for (String name : m.item().names()) {
            names.add(CodeBlock.of("$S", name));
        }
        if (m.item().descriptionKey().isEmpty() && m.item().description().isEmpty()) {
            if (m.isNullary()) {
                return CodeBlock.of(".addNullary($T.of($L))",
                        List.class, CodeBlock.join(names, ", "));
            } else {
                return CodeBlock.of(".addUnary($T.$L, $T.of($L), $S)",
                        Multiplicity.class, m.multiplicity().name(),
                        List.class, CodeBlock.join(names, ", "),
                        m.paramLabel());
            }
        }
        CodeBlock.Builder code = CodeBlock.builder();
        code.add(".addOption(");
        if (m.isNullary()) {
            code.add("$T.nullary()", net.jbock.model.Option.class);
            code.indent();
        } else {
            code.add("$T.unary($T.$L)", net.jbock.model.Option.class, Multiplicity.class, m.multiplicity().name());
            code.indent();
            code.add("\n.withParamLabel($S)", m.paramLabel());
        }
        m.item().descriptionKey().ifPresent(key -> code.add("\n.withDescriptionKey($S)", key));
        code.add("\n.withNames($T.of($L))", List.class, CodeBlock.join(names, ", "));
        for (String line : m.item().description()) {
            code.add(".addDescriptionLine($S)", line);
        }
        code.add("\n.build())");
        return code.unindent().build();
    }

    private CodeBlock parameterBlock(Mapping<net.jbock.annotated.Parameter> m) {
        if (m.item().descriptionKey().isEmpty() && m.item().description().isEmpty()) {
            return CodeBlock.of(".addParameter($T.$L, $S)",
                    Multiplicity.class, m.multiplicity().name(),
                    m.paramLabel());
        }
        CodeBlock.Builder code = CodeBlock.builder();
        code.add(".addParameter($T.builder($T.$L)", Parameter.class, Multiplicity.class, m.multiplicity().name());
        code.indent();
        code.add("\n.withParamLabel($S)", m.paramLabel());
        m.item().descriptionKey().ifPresent(key -> code.add("\n.withDescriptionKey($S)", key));
        for (String line : m.item().description()) {
            code.add("\n.addDescriptionLine($S)", line);
        }
        code.add("\n.build())");
        return code.unindent().build();
    }

    private CommandRepresentation cr() {
        return cr;
    }
}
