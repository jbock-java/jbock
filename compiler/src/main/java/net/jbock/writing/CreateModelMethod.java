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
final class CreateModelMethod implements HasCommandRepresentation {
    private final CommandRepresentation commandRepresentation;

    CreateModelMethod(CommandRepresentation commandRepresentation) {
        this.commandRepresentation = commandRepresentation;
    }

    private final Supplier<MethodSpec> define = memoize(() -> {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("return $T.builder()", CommandModel.class));
        sourceElement().descriptionKey().ifPresent(key ->
                code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String descriptionLine : sourceElement().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", descriptionLine));
        }
        code.add(CodeBlock.of(".withProgramName($S)", sourceElement().programName()));
        if (isSuperCommand()) {
            code.add(CodeBlock.of(".withSuperCommand($L)", true));
        }
        for (Mapping<Option> c : namedOptions()) {
            code.add(optionBlock(c));
        }
        for (Mapping<net.jbock.annotated.Parameter> c : positionalParameters()) {
            code.add(parameterBlock(c));
        }
        code.add(CodeBlock.of(".build()"));
        return methodBuilder("createModel")
                .addStatement(CodeBlock.join(code, System.lineSeparator()))
                .returns(CommandModel.class)
                .addModifiers(sourceElement().accessModifiers())
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
        List<CodeBlock> code = new ArrayList<>();
        if (m.isNullary()) {
            code.add(CodeBlock.of("$T.nullary()", net.jbock.model.Option.class));
        } else {
            code.add(CodeBlock.of("$T.unary($T.$L)", net.jbock.model.Option.class, Multiplicity.class, m.multiplicity().name()));
            code.add(CodeBlock.of(".withParamLabel($S)", m.paramLabel()));
        }
        m.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        code.add(CodeBlock.of(".withNames($T.of($L))", List.class, CodeBlock.join(names, ", ")));
        for (String line : m.item().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return CodeBlock.of(".addOption($L)", CodeBlock.join(code, System.lineSeparator()));
    }

    private CodeBlock parameterBlock(Mapping<?> m) {
        List<CodeBlock> code = new ArrayList<>();
        if (m.item().descriptionKey().isEmpty() && m.item().description().isEmpty()) {
            return CodeBlock.of(".addParameter($T.$L, $S)",
                    Multiplicity.class, m.multiplicity().name(),
                    m.paramLabel());
        }
        code.add(CodeBlock.of("$T.builder($T.$L)", Parameter.class, Multiplicity.class, m.multiplicity().name()));
        code.add(CodeBlock.of(".withParamLabel($S)", m.paramLabel()));
        m.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String line : m.item().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return CodeBlock.of(".addParameter($L)", CodeBlock.join(code, System.lineSeparator()));
    }

    @Override
    public CommandRepresentation commandRepresentation() {
        return commandRepresentation;
    }
}
