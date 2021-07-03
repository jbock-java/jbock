package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import net.jbock.common.SafeElements;
import net.jbock.common.Util;
import net.jbock.convert.Mapped;
import net.jbock.model.CommandModel;
import net.jbock.model.Multiplicity;
import net.jbock.model.Option;
import net.jbock.model.Parameter;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;

@ContextScope
public class CreateModelMethod extends CachedMethod {

    private final Util util;
    private final SourceElement sourceElement;
    private final NamedOptions namedOptions;
    private final PositionalParameters positionalParameters;
    private final SafeElements elements;

    @Inject
    CreateModelMethod(
            Util util,
            SourceElement sourceElement,
            NamedOptions namedOptions,
            PositionalParameters positionalParameters,
            SafeElements elements) {
        this.util = util;
        this.sourceElement = sourceElement;
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
        this.elements = elements;
    }

    @Override
    MethodSpec define() {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("return $T.builder()", CommandModel.class));
        sourceElement.descriptionKey().ifPresent(key ->
                code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        for (String descriptionLine : sourceElement.description(elements)) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", descriptionLine));
        }
        code.add(CodeBlock.of(".withProgramName($S)", sourceElement.programName()));
        if (!sourceElement.helpEnabled()) {
            code.add(CodeBlock.of(".withHelpEnabled($L)", false));
        }
        if (sourceElement.isSuperCommand()) {
            code.add(CodeBlock.of(".withSuperCommand($L)", true));
        }
        if (!sourceElement.atFileExpansion()) {
            code.add(CodeBlock.of(".withAtFileExpansion($L)", false));
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
                .addStatement(util.joinByNewline(code))
                .returns(CommandModel.class)
                .addModifiers(PRIVATE)
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
        code.add(CodeBlock.of(".withNames($T.of($L))", List.class, util.joinByComma(names)));
        if (c.isFlag()) {
            code.add(CodeBlock.of(".withModeFlag()"));
        } else if (c.multiplicity() != Multiplicity.OPTIONAL) {
            code.add(CodeBlock.of(".withMultiplicity($T.$L)", Multiplicity.class, c.multiplicity().name()));
        }
        for (String line : c.item().description(elements)) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return util.joinByNewline(code);
    }

    private CodeBlock parameterBlock(Mapped<PositionalParameter> c) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder()", Parameter.class));
        code.add(CodeBlock.of(".withParamLabel($S)", c.paramLabel()));
        c.item().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        if (c.multiplicity() != Multiplicity.REQUIRED) {
            code.add(CodeBlock.of(".withMultiplicity($T.$L)", Multiplicity.class, c.multiplicity().name()));
        }
        for (String line : c.item().description(elements)) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return util.joinByNewline(code);
    }
}
