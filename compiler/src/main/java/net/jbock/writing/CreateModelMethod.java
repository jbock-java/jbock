package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.Option;
import net.jbock.convert.Mapping;
import net.jbock.model.CommandModel;
import net.jbock.model.Multiplicity;
import net.jbock.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.jbock.javapoet.MethodSpec.methodBuilder;
import static net.jbock.common.Suppliers.memoize;
import static net.jbock.writing.CodeBlocks.joinByComma;
import static net.jbock.writing.CodeBlocks.joinByNewline;

@WritingScope
final class CreateModelMethod extends HasCommandRepresentation {

    @Inject
    CreateModelMethod(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
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
            code.add(CodeBlock.of(".addOption($L)", optionBlock(c)));
        }
        Stream.concat(positionalParameters().stream(), varargsParameter().stream())
                .forEach(c -> code.add(CodeBlock.of(".addParameter($L)", parameterBlock(c))));
        code.add(CodeBlock.of(".build()"));
        return methodBuilder("createModel")
                .addStatement(joinByNewline(code))
                .returns(CommandModel.class)
                .addModifiers(sourceElement().accessModifiers())
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
        List<CodeBlock> code = new ArrayList<>();
        if (m.isNullary()) {
            code.add(CodeBlock.of("$T.nullary()", net.jbock.model.Option.class));
        } else {
            code.add(CodeBlock.of("$T.unary($T.$L)", net.jbock.model.Option.class, Multiplicity.class, m.multiplicity().name()));
        }
        code.add(CodeBlock.of(".withParamLabel($S)", m.paramLabel()));
        m.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        code.add(CodeBlock.of(".withNames($T.of($L))", List.class, joinByComma(names)));
        for (String line : m.item().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return joinByNewline(code);
    }

    private CodeBlock parameterBlock(Mapping<?> m) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder($T.$L)", Parameter.class, Multiplicity.class, m.multiplicity().name()));
        code.add(CodeBlock.of(".withParamLabel($S)", m.paramLabel()));
        m.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String line : m.item().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return joinByNewline(code);
    }
}
