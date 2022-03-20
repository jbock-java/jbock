package net.jbock.writing;

import io.jbock.javapoet.ArrayTypeName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.TypeName;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.model.ItemType;
import net.jbock.processor.SourceElement;
import net.jbock.util.ExConvert;
import net.jbock.util.ExFailure;
import net.jbock.util.ExMissingItem;

import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.EITHERS;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ARRAY;
import static net.jbock.writing.CodeBlocks.joinByComma;
import static net.jbock.writing.CodeBlocks.joinByNewline;

@WritingScope
final class HarvestMethod extends Cached<MethodSpec> {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final List<Mapping<AnnotatedParameter>> positionalParameters;
    private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;
    private final ParserTypeFactory parserTypeFactory;
    private final ParameterSpec left = ParameterSpec.builder(STRING, "left").build();

    @Inject
    HarvestMethod(
            GeneratedTypes generatedTypes,
            CommandRepresentation commandRepresentation,
            ParserTypeFactory parserTypeFactory) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = commandRepresentation.sourceElement();
        this.namedOptions = commandRepresentation.namedOptions();
        this.positionalParameters = commandRepresentation.positionalParameters();
        this.repeatablePositionalParameters = commandRepresentation.repeatablePositionalParameters();
        this.parserTypeFactory = parserTypeFactory;
    }

    @Override
    MethodSpec define() {
        ParameterSpec parser = parserTypeFactory.define().asParam();
        CodeBlock constructorArguments = getConstructorArguments();
        MethodSpec.Builder spec = MethodSpec.methodBuilder("harvest");
        for (int i = 0; i < namedOptions.size(); i++) {
            Mapping<AnnotatedOption> m = namedOptions.get(i);
            ParameterSpec p = asParam(m);
            spec.addStatement("$T $N = $L", p.type, p, convertExpressionOption(m, i));
        }
        for (int i = 0; i < positionalParameters.size(); i++) {
            Mapping<AnnotatedParameter> m = positionalParameters.get(i);
            ParameterSpec p = asParam(m);
            spec.addStatement("$T $N = $L", p.type, p, convertExpressionRegularParameter(m, i));
        }
        repeatablePositionalParameters.forEach(m -> {
            ParameterSpec p = asParam(m);
            spec.addStatement("$T $N = $L", p.type, p, convertExpressionRepeatableParameter(m));
        });
        generatedTypes.superResultType().ifPresentOrElse(parseResultWithRestType -> {
                    ParameterSpec result = ParameterSpec.builder(sourceElement.typeName(), "result").build();
                    ParameterSpec restArgs = ParameterSpec.builder(sourceElement.typeName(), "restArgs").build();
                    spec.addStatement("$T $N = new $T($L)", result.type, result, generatedTypes.implType(),
                            constructorArguments);
                    spec.addStatement("$T $N = $N.rest().toArray($T::new)", STRING_ARRAY, restArgs,
                            parser, ArrayTypeName.of(String.class));
                    spec.addStatement("return new $T($N, $N)", parseResultWithRestType,
                            result, restArgs);
                },
                () -> spec.addStatement("return new $T($L)", generatedTypes.implType(), constructorArguments));
        return spec.returns(generatedTypes.parseSuccessType())
                .addParameter(parser)
                .addException(ExFailure.class)
                .addModifiers(PRIVATE)
                .build();
    }

    private CodeBlock getConstructorArguments() {
        List<CodeBlock> code = new ArrayList<>();
        for (Mapping<AnnotatedOption> m : namedOptions) {
            code.add(CodeBlock.of("$N", asParam(m)));
        }
        for (Mapping<AnnotatedParameter> m : positionalParameters) {
            code.add(CodeBlock.of("$N", asParam(m)));
        }
        repeatablePositionalParameters.stream()
                .map(m -> CodeBlock.of("$N", asParam(m)))
                .forEach(code::add);
        return joinByComma(code);
    }

    private CodeBlock convertExpressionOption(Mapping<AnnotatedOption> m, int i) {
        ParameterSpec parser = parserTypeFactory.define().asParam();
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.option($T.$N)", parser,
                sourceElement.optionEnumType(), m.enumName()));
        if (!m.isModeFlag()) {
            code.add(CodeBlock.of(".map($L)", m.mapper()));
        }
        code.addAll(tailExpressionOption(m, i));
        m.extractExpr().ifPresent(code::add);
        return joinByNewline(code);
    }

    private CodeBlock convertExpressionRegularParameter(Mapping<AnnotatedParameter> m, int i) {
        ParameterSpec parser = parserTypeFactory.define().asParam();
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.param($L)", parser,
                m.sourceMethod().index()));
        code.add(CodeBlock.of(".map($L)", m.mapper()));
        code.addAll(tailExpressionParameter(m, i));
        m.extractExpr().ifPresent(code::add);
        return joinByNewline(code);
    }

    private CodeBlock convertExpressionRepeatableParameter(Mapping<AnnotatedParameters> m) {
        ParameterSpec parser = parserTypeFactory.define().asParam();
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.rest()", parser));
        code.add(CodeBlock.of(".map($L)", m.mapper()));
        code.add(CodeBlock.of(".collect($T.firstFailure())", EITHERS));
        code.add(orElseThrowConverterError(ItemType.PARAMETER, positionalParameters.size()));
        return joinByNewline(code);
    }

    private List<CodeBlock> tailExpressionOption(Mapping<AnnotatedOption> m, int i) {
        if (m.isModeFlag()) {
            return List.of(CodeBlock.of(".findAny().isPresent()"));
        }
        switch (m.multiplicity()) {
            case REQUIRED:
                return List.of(
                        CodeBlock.of(".findAny()"),
                        CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                                ExMissingItem.class, ItemType.class, ItemType.OPTION, i),
                        orElseThrowConverterError(ItemType.OPTION, i));
            case OPTIONAL:
                return List.of(
                        CodeBlock.of(".collect($T.firstFailure())", EITHERS),
                        orElseThrowConverterError(ItemType.OPTION, i),
                        CodeBlock.of(".stream().findAny()"));
            default: {
                if (!m.isRepeatable()) {
                    throw new AssertionError();
                }
                return List.of(
                        CodeBlock.of(".collect($T.firstFailure())", EITHERS),
                        orElseThrowConverterError(ItemType.OPTION, i));
            }
        }
    }

    private List<CodeBlock> tailExpressionParameter(Mapping<AnnotatedParameter> m, int i) {
        if (m.isRequired()) {
            return List.of(CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                            ExMissingItem.class, ItemType.class, ItemType.PARAMETER, i),
                    orElseThrowConverterError(ItemType.PARAMETER, i));
        }
        if (!m.isOptional()) {
            throw new AssertionError();
        }
        return List.of(
                CodeBlock.of(".stream()"),
                CodeBlock.of(".collect($T.firstFailure())", EITHERS),
                orElseThrowConverterError(ItemType.PARAMETER, i),
                CodeBlock.of(".stream().findAny()"));
    }

    private CodeBlock orElseThrowConverterError(ItemType itemType, int i) {
        return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5L))",
                left, ExConvert.class, ItemType.class, itemType, i);
    }

    private ParameterSpec asParam(Mapping<?> mapping) {
        TypeName type = TypeName.get(mapping.sourceMethod().returnType());
        String name = mapping.sourceMethod().methodName();
        return ParameterSpec.builder(type, '_' + name).build();
    }
}
