package net.jbock.writing;

import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.model.CommandModel;
import net.jbock.model.Multiplicity;
import net.jbock.model.Option;
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
        if (sourceElement().isSuperCommand()) {
            code.add(CodeBlock.of(".withSuperCommand($L)", true));
        }
        for (Mapping<AnnotatedOption> c : namedOptions()) {
            code.add(CodeBlock.of(".addOption($L)", optionBlock(c)));
        }
        Stream.of(positionalParameters(), repeatablePositionalParameters())
                .flatMap(List::stream)
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

    private CodeBlock optionBlock(Mapping<AnnotatedOption> m) {
        List<CodeBlock> names = new ArrayList<>();
        for (String name : m.sourceMethod().names()) {
            names.add(CodeBlock.of("$S", name));
        }
        List<CodeBlock> code = new ArrayList<>();
        if (m.isModeFlag()) {
            code.add(CodeBlock.of("$T.nullary()", Option.class));
        } else {
            code.add(CodeBlock.of("$T.unary($T.$L)", Option.class, Multiplicity.class, m.multiplicity().name()));
        }
        code.add(CodeBlock.of(".withParamLabel($S)", m.paramLabel()));
        m.sourceMethod().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        code.add(CodeBlock.of(".withNames($T.of($L))", List.class, joinByComma(names)));
        for (String line : m.sourceMethod().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return joinByNewline(code);
    }

    private CodeBlock parameterBlock(Mapping<?> m) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder($T.$L)", Parameter.class, Multiplicity.class, m.multiplicity().name()));
        code.add(CodeBlock.of(".withParamLabel($S)", m.paramLabel()));
        m.sourceMethod().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String line : m.sourceMethod().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return joinByNewline(code);
    }
}
