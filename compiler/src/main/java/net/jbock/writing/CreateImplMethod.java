package net.jbock.writing;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.common.Suppliers;
import net.jbock.contrib.StandardConverters;
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
import static net.jbock.common.Constants.CONVERTERS;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.STRING;

/**
 * Implementation of the command interface.
 */
final class CreateImplMethod implements HasCommandRepresentation {

    private final CommandRepresentation commandRepresentation;
    private final GeneratedTypes generatedTypes;

    private final Supplier<ParameterSpec> resultSupplier = Suppliers.memoize(() ->
            ParameterSpec.builder(ClassName.get(ParseResult.class), "result").build());

    @Inject
    CreateImplMethod(
            CommandRepresentation commandRepresentation,
            GeneratedTypes generatedTypes) {
        this.commandRepresentation = commandRepresentation;
        this.generatedTypes = generatedTypes;
    }

    MethodSpec define() {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("createImpl").addModifiers(PRIVATE, STATIC);
        List<CodeBlock> constructorParams = new ArrayList<>(namedOptions().size() + positionalParameters().size() + 1);
        if (allMappings().stream().anyMatch(m -> m.createConverterExpression().isPresent())) {
            ParameterSpec converters = ParameterSpec.builder(StandardConverters.class, CONVERTERS).build();
            spec.addStatement("$1T $2N = new $1T()", StandardConverters.class, converters);
        }
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
        spec.addStatement("return new $T($L)", generatedTypes.implType(), CodeBlock.join(constructorParams, ", "));
        return spec.addParameter(result())
                .returns(sourceElement().typeName())
                .addException(ExFailure.class)
                .build();
    }

    private CodeBlock convertExpressionOption(Mapping<Option> m, int i) {
        List<CodeBlock> code = new ArrayList<>();
        if (m.isNullary()) {
            code.add(CodeBlock.of("!$N.option($L).isEmpty()", result(),
                    m.item().internalIndex()));
        } else if (m.createConverterExpression().isPresent()) {
            code.add(CodeBlock.of("$N.option($L).stream()", result(),
                    m.item().internalIndex()));
            code.add(CodeBlock.of(".map($L)", m.createConverterExpression().orElseThrow()));
            code.addAll(tailExpressionOption(m, i));
        } else {
            code.add(CodeBlock.of("$N.option($L)$L", result(),
                    m.item().internalIndex(), tailExpressionStringOption(m, i)));
        }
        m.extractExpr().ifPresent(code::add);
        return CodeBlock.join(code, System.lineSeparator());
    }

    private CodeBlock convertExpressionParameter(Mapping<Parameter> m, int i) {
        List<CodeBlock> code = new ArrayList<>();
        if (m.isRepeatable()) {
            return convertExpressionVarargsParameter(m);
        }
        if (m.createConverterExpression().isPresent()) {
            code.add(CodeBlock.of("$N.param($L)", result(),
                    m.item().index()));
            code.add(CodeBlock.of(".map($L)", m.createConverterExpression().orElseThrow()));
            code.addAll(tailExpressionParameter(m, i));
        } else {
            code.add(CodeBlock.of("$N.param($L)$L", result(),
                    m.item().index(), tailExpressionStringParameter(m, i)));
        }
        m.extractExpr().ifPresent(code::add);
        return CodeBlock.join(code, System.lineSeparator());
    }

    private CodeBlock convertExpressionVarargsParameter(Mapping<Parameter> m) {
        List<CodeBlock> code = new ArrayList<>();
        if (m.createConverterExpression().isPresent()) {
            code.add(CodeBlock.of("$N.rest().stream()", result()));
            code.add(CodeBlock.of(".map($L)", m.createConverterExpression().orElseThrow()));
            code.add(CodeBlock.of(".collect($T.firstFailure())", EITHER));
            code.add(orElseThrowConverterError(ItemType.PARAMETER, positionalParameters().size()));
        } else {
            code.add(CodeBlock.of("$N.rest()", result()));
        }
        return CodeBlock.join(code, System.lineSeparator());
    }

    private CodeBlock tailExpressionStringOption(Mapping<Option> m, int i) {
        return switch (m.multiplicity()) {
            case REQUIRED -> CodeBlock.of(".stream().findAny().orElseThrow(() -> new $T($T.$L, $L))",
                    ExMissingItem.class, ItemType.class, ItemType.OPTION, i);
            case OPTIONAL -> CodeBlock.of(".stream().findAny()");
            case REPEATABLE -> CodeBlock.builder().build();
        };
    }

    private List<CodeBlock> tailExpressionOption(Mapping<Option> m, int i) {
        return switch (m.multiplicity()) {
            case REQUIRED -> List.of(
                    CodeBlock.of(".findAny()"),
                    CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                            ExMissingItem.class, ItemType.class, ItemType.OPTION, i),
                    orElseThrowConverterError(ItemType.OPTION, i));
            case OPTIONAL -> List.of(
                    CodeBlock.of(".collect($T.firstFailure())", EITHER),
                    orElseThrowConverterError(ItemType.OPTION, i),
                    CodeBlock.of(".stream().findAny()"));
            case REPEATABLE -> List.of(
                    CodeBlock.of(".collect($T.firstFailure())", EITHER),
                    orElseThrowConverterError(ItemType.OPTION, i));
        };
    }

    private List<CodeBlock> tailExpressionParameter(Mapping<Parameter> m, int i) {
        return switch (m.multiplicity()) {
            case REQUIRED -> List.of(CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                            ExMissingItem.class, ItemType.class, ItemType.PARAMETER, i),
                    orElseThrowConverterError(ItemType.PARAMETER, i));
            case OPTIONAL -> List.of(CodeBlock.of(".stream()"),
                    CodeBlock.of(".collect($T.firstFailure())", EITHER),
                    orElseThrowConverterError(ItemType.PARAMETER, i),
                    CodeBlock.of(".stream().findAny()"));
            default-> throw new AssertionError("repeatable handled elsewhere");
        };
    }

    private CodeBlock tailExpressionStringParameter(Mapping<Parameter> m, int i) {
        return switch (m.multiplicity()) {
            case REQUIRED -> CodeBlock.of(".orElseThrow(() -> new $T($T.$L, $L))",
                    ExMissingItem.class, ItemType.class, ItemType.PARAMETER, i);
            case OPTIONAL -> CodeBlock.builder().build();
            default-> throw new AssertionError("repeatable handled elsewhere");
        };
    }

    private ParameterSpec result() {
        return resultSupplier.get();
    }

    private CodeBlock orElseThrowConverterError(ItemType itemType, int i) {
        ParameterSpec failure = ParameterSpec.builder(STRING, "failure").build();
        return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5L))",
                failure, ExConvert.class, ItemType.class, itemType, i);
    }

    @Override
    public CommandRepresentation commandRepresentation() {
        return commandRepresentation;
    }
}
