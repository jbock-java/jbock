package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.model.CommandModel;
import net.jbock.model.Multiplicity;
import net.jbock.model.Option;
import net.jbock.model.Parameter;
import net.jbock.processor.SourceElement;
import net.jbock.util.ParseRequest;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.concat;

@ContextScope
public class CreateModelMethod extends CachedMethod {

    private final ContextUtil contextUtil;
    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final UnixClustering options;
    private final List<Mapping<AnnotatedParameter>> positionalParameters;
    private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;

    @Inject
    CreateModelMethod(
            ContextUtil contextUtil,
            SourceElement sourceElement,
            List<Mapping<AnnotatedOption>> namedOptions,
            UnixClustering options,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters) {
        this.contextUtil = contextUtil;
        this.sourceElement = sourceElement;
        this.namedOptions = namedOptions;
        this.options = options;
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
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
        if (options.unixClusteringSupported()) {
            code.add(CodeBlock.of(".withUnixClustering($L)", true));
        }
        for (Mapping<AnnotatedOption> c : namedOptions) {
            code.add(CodeBlock.of(".addOption($L)", optionBlock(c)));
        }
        for (Mapping<?> c : concat(positionalParameters, repeatablePositionalParameters)) {
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

    private CodeBlock optionBlock(Mapping<AnnotatedOption> c) {
        List<CodeBlock> names = new ArrayList<>();
        for (String name : c.sourceMethod().names()) {
            names.add(CodeBlock.of("$S", name));
        }
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder()", Option.class));
        code.add(CodeBlock.of(".withParamLabel($S)", c.paramLabel()));
        c.sourceMethod().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        code.add(CodeBlock.of(".withNames($T.of($L))", List.class, contextUtil.joinByComma(names)));
        if (c.modeFlag()) {
            code.add(CodeBlock.of(".withModeFlag()"));
        } else if (c.multiplicity() != Multiplicity.OPTIONAL) {
            code.add(CodeBlock.of(".withMultiplicity($T.$L)", Multiplicity.class, c.multiplicity().name()));
        }
        for (String line : c.sourceMethod().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return contextUtil.joinByNewline(code);
    }

    private CodeBlock parameterBlock(Mapping<?> c) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.builder()", Parameter.class));
        code.add(CodeBlock.of(".withParamLabel($S)", c.paramLabel()));
        c.sourceMethod().descriptionKey().ifPresent(key -> code.add(CodeBlock.of(".withDescriptionKey($S)", key)));
        if (c.multiplicity() != Multiplicity.REQUIRED) {
            code.add(CodeBlock.of(".withMultiplicity($T.$L)", Multiplicity.class, c.multiplicity().name()));
        }
        for (String line : c.sourceMethod().description()) {
            code.add(CodeBlock.of(".addDescriptionLine($S)", line));
        }
        code.add(CodeBlock.of(".build()"));
        return contextUtil.joinByNewline(code);
    }
}
