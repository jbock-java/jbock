package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;
import net.jbock.util.ExConvert;
import net.jbock.util.ExMissingItem;
import net.jbock.util.ExNotSuccess;
import net.jbock.util.ItemType;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static net.jbock.common.Constants.EITHERS;
import static net.jbock.common.Constants.OPTIONAL;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class BuildMethod extends CachedMethod {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final NamedOptions namedOptions;
    private final PositionalParameters positionalParameters;
    private final CommonFields commonFields;
    private final ContextUtil contextUtil;
    private final ParameterSpec left = ParameterSpec.builder(STRING, "left").build();

    @Inject
    BuildMethod(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            NamedOptions namedOptions,
            PositionalParameters positionalParameters,
            CommonFields commonFields,
            ContextUtil contextUtil) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
        this.commonFields = commonFields;
        this.contextUtil = contextUtil;
    }

    @Override
    MethodSpec define() {
        CodeBlock constructorArguments = getConstructorArguments();
        MethodSpec.Builder spec = MethodSpec.methodBuilder("build");
        List<Mapped<NamedOption>> options = namedOptions.options();
        for (int i = 0; i < options.size(); i++) {
            Mapped<NamedOption> c = options.get(i);
            ParameterSpec p = c.asParam();
            spec.addStatement("$T $N = $L", p.type, p, convertExpressionOption(c, i));
        }
        List<Mapped<PositionalParameter>> regular = positionalParameters.regular();
        for (int i = 0, regularSize = regular.size(); i < regularSize; i++) {
            Mapped<PositionalParameter> c = regular.get(i);
            ParameterSpec p = c.asParam();
            spec.addStatement("$T $N = $L", p.type, p, convertExpressionRegularParameter(c, i));
        }
        positionalParameters.repeatable().forEach(c -> {
            ParameterSpec p = c.asParam();
            spec.addStatement("$T $N = $L", p.type, p, convertExpressionRepeatableParameter(c));
        });
        generatedTypes.superResultType().ifPresentOrElse(parseResultWithRestType -> {
                    ParameterSpec result = ParameterSpec.builder(sourceElement.typeName(), "result").build();
                    ParameterSpec restArgs = ParameterSpec.builder(sourceElement.typeName(), "restArgs").build();
                    spec.addStatement("$T $N = new $T($L)", result.type, result, generatedTypes.implType(),
                            constructorArguments);
                    spec.addStatement("$T $N = this.$N.toArray(new $T[0])", STRING_ARRAY, restArgs,
                            commonFields.rest(), STRING);
                    spec.addStatement("return new $T($N, $N)", parseResultWithRestType,
                            result, restArgs);
                },
                () -> spec.addStatement("return new $T($L)", generatedTypes.implType(), constructorArguments));
        return spec.returns(generatedTypes.parseSuccessType())
                .addException(ExNotSuccess.class)
                .build();
    }

    private CodeBlock getConstructorArguments() {
        List<CodeBlock> code = new ArrayList<>();
        for (Mapped<NamedOption> c : namedOptions.options()) {
            code.add(CodeBlock.of("$N", c.asParam()));
        }
        for (Mapped<PositionalParameter> c : positionalParameters.regular()) {
            code.add(CodeBlock.of("$N", c.asParam()));
        }
        positionalParameters.repeatable().stream()
                .map(c -> CodeBlock.of("$N", c.asParam()))
                .forEach(code::add);
        return contextUtil.joinByComma(code);
    }

    private CodeBlock convertExpressionOption(Mapped<NamedOption> c, int i) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("this.$N.get($T.$N).stream()", commonFields.optionParsers(),
                sourceElement.optionEnumType(), c.enumName().enumConstant()));
        if (!c.isFlag()) {
            code.add(CodeBlock.of(".map($L)", c.simpleMapExpr()
                    .orElseGet(() -> CodeBlock.of("new $T()", generatedTypes.multilineConverterType(c)))));
        }
        code.addAll(tailExpressionOption(c, i));
        c.extractExpr().ifPresent(code::add);
        return contextUtil.joinByNewline(code);
    }

    private CodeBlock convertExpressionRegularParameter(Mapped<PositionalParameter> c, int i) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$T.ofNullable(this.$N[$L])", OPTIONAL, commonFields.params(),
                c.item().index()));
        code.add(CodeBlock.of(".map($L)", c.simpleMapExpr()
                .orElseGet(() -> CodeBlock.of("new $T()", generatedTypes.multilineConverterType(c)))));
        code.addAll(tailExpressionParameter(c, i));
        c.extractExpr().ifPresent(code::add);
        return contextUtil.joinByNewline(code);
    }

    private CodeBlock convertExpressionRepeatableParameter(Mapped<PositionalParameter> c) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("this.$N.stream()", commonFields.rest()));
        code.add(CodeBlock.of(".map($L)", c.simpleMapExpr()
                .orElseGet(() -> CodeBlock.of("new $T()", generatedTypes.multilineConverterType(c)))));
        code.add(CodeBlock.of(".collect($T.toValidList())", EITHERS));
        code.add(orElseThrowConverterError(ItemType.PARAMETER, positionalParameters.regular().size()));
        return contextUtil.joinByNewline(code);
    }

    private List<CodeBlock> tailExpressionOption(Mapped<NamedOption> c, int i) {
        if (c.isFlag()) {
            return List.of(CodeBlock.of(".findAny().isPresent()"));
        }
        switch (c.multiplicity()) {
            case REQUIRED:
                return List.of(
                        CodeBlock.of(".findAny()"),
                        CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                                ExMissingItem.class, ItemType.class, ItemType.OPTION, i),
                        orElseThrowConverterError(ItemType.OPTION, i));
            case OPTIONAL:
                return List.of(
                        CodeBlock.of(".collect($T.toValidList())", EITHERS),
                        orElseThrowConverterError(ItemType.OPTION, i),
                        CodeBlock.of(".stream().findAny()"));
            case REPEATABLE:
                return List.of(
                        CodeBlock.of(".collect($T.toValidList())", EITHERS),
                        orElseThrowConverterError(ItemType.OPTION, i));
            default:
                throw new IllegalArgumentException("unexpected multiplicity: " + c.multiplicity());
        }
    }

    private List<CodeBlock> tailExpressionParameter(Mapped<PositionalParameter> c, int i) {
        switch (c.multiplicity()) {
            case REQUIRED:
                return List.of(CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                        ExMissingItem.class, ItemType.class, ItemType.PARAMETER, i),
                        orElseThrowConverterError(ItemType.PARAMETER, i));
            case OPTIONAL:
                return List.of(
                        CodeBlock.of(".stream()"),
                        CodeBlock.of(".collect($T.toValidList())", EITHERS),
                        orElseThrowConverterError(ItemType.PARAMETER, i),
                        CodeBlock.of(".stream().findAny()"));
            default:
                throw new IllegalArgumentException("unexpected multiplicity: " + c.multiplicity());
        }
    }

    private CodeBlock orElseThrowConverterError(ItemType itemType, int i) {
        return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5L))",
                left, ExConvert.class, ItemType.class, itemType, i);
    }
}
