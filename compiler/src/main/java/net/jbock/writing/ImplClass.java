package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.CodeBlock;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.ParameterSpec;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import net.jbock.annotated.Item;
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

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.EITHERS;
import static net.jbock.common.Constants.STRING;
import static net.jbock.writing.CodeBlocks.joinByNewline;

/**
 * Implementation of the command class.
 */
final class ImplClass extends HasCommandRepresentation {

    private final GeneratedTypes generatedTypes;

    ImplClass(GeneratedTypes generatedTypes,
              CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
        this.generatedTypes = generatedTypes;
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
        if (sourceElement().isInterface()) {
            spec.addSuperinterface(sourceElement().typeName());
        } else {
            spec.superclass(sourceElement().typeName());
        }
        return spec.addModifiers(PRIVATE, STATIC, FINAL)
                .addMethod(constructor())
                .addFields(allMappings().stream()
                        .map(Mapping::field)
                        .collect(toList()))
                .addMethods(allMappings().stream()
                        .map(this::parameterMethodOverride)
                        .collect(toList()))
                .build();
    }

    private MethodSpec parameterMethodOverride(Mapping<?> m) {
        Item sourceMethod = m.item();
        return MethodSpec.methodBuilder(sourceMethod.methodName())
                .returns(TypeName.get(sourceMethod.returnType()))
                .addModifiers(sourceMethod.accessModifiers())
                .addStatement("return $N", m.field())
                .addAnnotation(Override.class)
                .build();
    }

    private final Supplier<ParameterSpec> resultSupplier = Suppliers.memoize(() -> {
        ParameterizedTypeName resultType = ParameterizedTypeName.get(ClassName.get(ParseResult.class),
                optType());
        return ParameterSpec.builder(resultType, "result").build();
    });

    private ParameterSpec result() {
        return resultSupplier.get();
    }

    private MethodSpec constructor() {
        MethodSpec.Builder spec = MethodSpec.constructorBuilder();
        for (int i = 0; i < namedOptions().size(); i++) {
            Mapping<Option> m = namedOptions().get(i);
            spec.addStatement("this.$N = $L", m.field(), convertExpressionOption(m, i));
        }
        for (int i = 0; i < positionalParameters().size(); i++) {
            Mapping<Parameter> m = positionalParameters().get(i);
            spec.addStatement("this.$N = $L", m.field(), convertExpressionParameter(m, i));
        }
        varargsParameter().ifPresent(m ->
                spec.addStatement("this.$N = $L", m.field(), convertExpressionVarargsParameter(m)));
        return spec.addParameter(result())
                .addException(ExFailure.class)
                .build();
    }

    private CodeBlock convertExpressionOption(Mapping<Option> m, int i) {
        List<CodeBlock> code = new ArrayList<>();
        code.add(CodeBlock.of("$N.option($T.$N)", result(),
                sourceElement().optionEnumType(), m.enumName()));
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
        code.add(CodeBlock.of(".collect($T.firstFailure())", EITHERS));
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
                CodeBlock.of(".collect($T.firstFailure())", EITHERS),
                orElseThrowConverterError(ItemType.PARAMETER, i),
                CodeBlock.of(".stream().findAny()"));
    }

    private CodeBlock orElseThrowConverterError(ItemType itemType, int i) {
        ParameterSpec left = ParameterSpec.builder(STRING, "left").build();
        return CodeBlock.of(".orElseThrow($1N -> new $2T($1N, $3T.$4L, $5L))",
                left, ExConvert.class, ItemType.class, itemType, i);
    }
}
