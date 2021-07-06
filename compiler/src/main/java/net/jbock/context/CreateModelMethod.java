package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.convert.Mapped;
import net.jbock.model.CommandModel;
import net.jbock.model.Multiplicity;
import net.jbock.model.Option;
import net.jbock.model.Parameter;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;
import net.jbock.util.ParseRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;

@ContextScope
public class CreateModelMethod extends CachedMethod {

    private final ContextUtil contextUtil;
    private final SourceElement sourceElement;
    private final NamedOptions namedOptions;
    private final PositionalParameters positionalParameters;

    @Inject
    CreateModelMethod(
            ContextUtil contextUtil,
            SourceElement sourceElement,
            NamedOptions namedOptions,
            PositionalParameters positionalParameters) {
        this.contextUtil = contextUtil;
        this.sourceElement = sourceElement;
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
    }

    @Override
    MethodSpec define() {
        List<CodeBlock> code = new ArrayList<>();
        ParameterSpec request = ParameterSpec.builder(ParseRequest.class, "request").build();
        code.add(CodeBlock.of("return $T.builder($N)", CommandModel.class, request));
        sourceElement.descriptionKey().ifPresent(key ->
                code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String descriptionLine : sourceElement.description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", descriptionLine));
        }
        code.add(CodeBlock.of(".withProgramName($S)", sourceElement.programName()));
        if (sourceElement.isSuperCommand()) {
            code.add(CodeBlock.of(".withSuperCommand($L)", true));
        }
        if (namedOptions.unixClusteringSupported()) {
            code.add(CodeBlock.of(".withUnixClustering($L)", true));
        }
        for (Mapped<NamedOption> c : namedOptions.options()) {
            code.add(CodeBlock.of(".addOption($L)", optionBlock(c)));
        }
        for (Mapped<PositionalParameter> c : positionalParameters.parameters()) {
            code.add(CodeBlock.of(".addParameter($L)", parameterBlock(c)));
        }
        code.add(CodeBlock.of(".build()"));
        return methodBuilder("createModel")
                .addStatement(contextUtil.joinByNewline(code))
                .returns(CommandModel.class)
                .addModifiers(PRIVATE)
                .addParameter(request)
                .build();
    }

    private CodeBlock optionBlock(Mapped<NamedOption> c) {
        List<CodeBlock> names = new ArrayList<>();
        for (String name : c.item().names()) {
            names.add(CodeBlock.of("$S", name));
        }
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder()", Option.class));
        code.add(CodeBlock.of(".withParamLabel($S)", c.paramLabel()));
        c.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        code.add(CodeBlock.of(".withNames($T.of($L))", List.class, contextUtil.joinByComma(names)));
        if (c.isFlag()) {
            code.add(CodeBlock.of(".withModeFlag()"));
        } else if (c.multiplicity() != Multiplicity.OPTIONAL) {
            code.add(CodeBlock.of(".withMultiplicity($T.$L)", Multiplicity.class, c.multiplicity().name()));
        }
        for (String line : c.item().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return contextUtil.joinByNewline(code);
    }

    private CodeBlock parameterBlock(Mapped<PositionalParameter> c) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder()", Parameter.class));
        code.add(CodeBlock.of(".withParamLabel($S)", c.paramLabel()));
        c.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        if (c.multiplicity() != Multiplicity.REQUIRED) {
            code.add(CodeBlock.of(".withMultiplicity($T.$L)", Multiplicity.class, c.multiplicity().name()));
        }
        for (String line : c.item().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return contextUtil.joinByNewline(code);
    }
}
