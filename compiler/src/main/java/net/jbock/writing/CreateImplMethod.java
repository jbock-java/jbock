package net.jbock.writing;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.annotated.VarargsParameter;
import net.jbock.common.Suppliers;
import net.jbock.convert.Mapping;
import net.jbock.model.ItemType;
import net.jbock.parse.ParseResult;
import net.jbock.util.ExConvert;
import net.jbock.util.ExFailure;
import net.jbock.util.ExMissingItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.STRING;
import static net.jbock.writing.CodeBlocks.joinByNewline;

/**
 * Implementation of the command class.
 */
final class CreateImplMethod extends HasCommandRepresentation {

    //   if the command class is an interface, we can generate a record instead:
    //
    //   static ComplicatedMapperArguments_Impl create_ComplicatedMapperArguments_Impl(ParseResult result) throws ExFailure {
    //       Integer number = result.option(0)
    //               .map(new ComplicatedMapperArguments.MyConverter().get())
    //               .findAny()
    //               .orElseThrow(() -> new ExMissingItem(ItemType.OPTION, 0))
    //               .orElseThrow(left -> new ExConvert(left, ItemType.OPTION, 0));
    //       List<ComplicatedMapperArguments.LazyNumber> numbers = result.option(1)
    //               .map(new ComplicatedMapperArguments.LazyNumberConverter().get())
    //               .collect(Eithers.firstFailure())
    //               .orElseThrow(left -> new ExConvert(left, ItemType.OPTION, 1));
    //       Optional<LocalDate> date = result.option(2)
    //               .map(new ComplicatedMapperArguments.NullReturningConverter())
    //               .collect(Eithers.firstFailure())
    //               .orElseThrow(left -> new ExConvert(left, ItemType.OPTION, 2))
    //               .stream().findAny();
    //       return new ComplicatedMapperArguments_Impl(number, numbers, date);
    //   }
    //
    //   record ComplicatedMapperArguments_Impl(
    //           Integer number,
    //           List<ComplicatedMapperArguments.LazyNumber> numbers,
    //           Optional<LocalDate> date) implements ComplicatedMapperArguments {
    //   }
    private final GeneratedTypes generatedTypes;

    @Inject
    CreateImplMethod(
            CommandRepresentation commandRepresentation,
            GeneratedTypes generatedTypes) {
        super(commandRepresentation);
        this.generatedTypes = generatedTypes;
    }

    MethodSpec define() {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("createImpl").addModifiers(PRIVATE, STATIC);
        List<CodeBlock> constructorParams = new ArrayList<>(namedOptions().size() + positionalParameters().size() + varargsParameter().map(x -> 1).orElse(0));
        for (int i = 0; i < namedOptions().size(); i++) {
            Mapping<Option> m = namedOptions().get(i);
            spec.addStatement("$T $N = $L", m.item().returnType(), m.param(), convertExpressionOption(m, i));
            constructorParams.add(CodeBlock.of("$N", m.param()));
        }
        for (int i = 0; i < positionalParameters().size(); i++) {
            Mapping<Parameter> m = positionalParameters().get(i);
            spec.addStatement("$T $N = $L", m.item().returnType(), m.param(), convertExpressionParameter(m, i));
            constructorParams.add(CodeBlock.of("$N", m.param()));
        }
        varargsParameter().ifPresent(m -> {
            spec.addStatement("$T $N = $L", m.item().returnType(), m.param(), convertExpressionVarargsParameter(m));
            constructorParams.add(CodeBlock.of("$N", m.param()));
        });
        spec.addStatement("return new $T($L)", generatedTypes.implType(), CodeBlocks.joinByComma(constructorParams));
        return spec.addParameter(result())
                .returns(sourceElement().typeName())
                .addException(ExFailure.class)
                .build();
    }

    private CodeBlock convertExpressionOption(Mapping<Option> m, int i) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.option($L)", result(),
                m.item().index()));
        if (!m.isNullary()) {
            code.add(CodeBlock.of(".map($L)", m.createConverterExpression()));
        }
        code.addAll(tailExpressionOption(m, i));
        m.extractExpr().ifPresent(code::add);
        return joinByNewline(code);
    }

    private CodeBlock convertExpressionParameter(Mapping<Parameter> m, int i) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.param($L)", result(),
                m.item().index()));
        code.add(CodeBlock.of(".map($L)", m.createConverterExpression()));
        code.addAll(tailExpressionParameter(m, i));
        m.extractExpr().ifPresent(code::add);
        return joinByNewline(code);
    }

    private CodeBlock convertExpressionVarargsParameter(Mapping<VarargsParameter> m) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.rest()", result()));
        code.add(CodeBlock.of(".map($L)", m.createConverterExpression()));
        code.add(CodeBlock.of(".collect($T.firstFailure())", EITHER));
        code.add(orElseThrowConverterError(ItemType.PARAMETER, positionalParameters().size()));
        return joinByNewline(code);
    }

    private List<CodeBlock> tailExpressionOption(Mapping<Option> m, int i) {
        if (m.isNullary()) {
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
                        CodeBlock.of(".collect($T.firstFailure())", EITHER),
                        orElseThrowConverterError(ItemType.OPTION, i),
                        CodeBlock.of(".stream().findAny()"));
            default: {
                if (!m.isRepeatable()) {
                    throw new AssertionError();
                }
                return List.of(
                        CodeBlock.of(".collect($T.firstFailure())", EITHER),
                        orElseThrowConverterError(ItemType.OPTION, i));
            }
        }
    }

    private List<CodeBlock> tailExpressionParameter(Mapping<Parameter> m, int i) {
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
                CodeBlock.of(".collect($T.firstFailure())", EITHER),
                orElseThrowConverterError(ItemType.PARAMETER, i),
                CodeBlock.of(".stream().findAny()"));
    }

    private final Supplier<ParameterSpec> resultSupplier = Suppliers.memoize(() ->
            ParameterSpec.builder(ClassName.get(ParseResult.class), "result").build());

    private ParameterSpec result() {
        return resultSupplier.get();
    }

    private CodeBlock orElseThrowConverterError(ItemType itemType, int i) {
        ParameterSpec left = ParameterSpec.builder(STRING, "left").build();
        return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5L))",
                left, ExConvert.class, ItemType.class, itemType, i);
    }
}
