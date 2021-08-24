package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.state.OptionStateModeFlag;
import net.jbock.state.OptionStateRegular;
import net.jbock.state.OptionStateRepeatable;

import javax.inject.Inject;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the inner class StatefulParser
 */
@ContextScope
public class StatefulParser {

    private final StatefulParseMethod statefulParseMethod;
    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final List<Mapping<AnnotatedParameter>> positionalParameters;
    private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;
    private final CommonFields commonFields;
    private final BuildMethod buildMethod;
    private final TryReadOptionMethod tryParseOptionMethod;

    @Inject
    StatefulParser(
            GeneratedTypes generatedTypes,
            StatefulParseMethod statefulParseMethod,
            SourceElement sourceElement,
            List<Mapping<AnnotatedOption>> namedOptions,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters,
            CommonFields commonFields,
            BuildMethod buildMethod,
            TryReadOptionMethod tryParseOptionMethod) {
        this.generatedTypes = generatedTypes;
        this.statefulParseMethod = statefulParseMethod;
        this.sourceElement = sourceElement;
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.commonFields = commonFields;
        this.buildMethod = buildMethod;
        this.tryParseOptionMethod = tryParseOptionMethod;
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.statefulParserType())
                .addModifiers(PRIVATE, STATIC)
                .addMethod(statefulParseMethod.define());
        spec.addField(commonFields.suspiciousPattern());
        if (!namedOptions.isEmpty()) {
            spec.addMethod(tryParseOptionMethod.get());
            spec.addMethod(privateConstructor());
            spec.addField(commonFields.optionNames());
            spec.addField(commonFields.optionParsers());
        }
        if (!positionalParameters.isEmpty()) {
            spec.addField(commonFields.params());
        }
        if (!repeatablePositionalParameters.isEmpty() || sourceElement.isSuperCommand()) {
            spec.addField(commonFields.rest());
        }
        spec.addMethod(buildMethod.get());
        return spec.build();
    }

    private MethodSpec privateConstructor() {
        CodeBlock.Builder code = CodeBlock.builder();
        for (Mapping<AnnotatedOption> namedOption : namedOptions) {
            String enumConstant = namedOption.enumName().enumConstant();
            for (String dashedName : namedOption.sourceMethod().names()) {
                code.addStatement("$N.put($S, $T.$L)",
                        commonFields.optionNames(), dashedName, sourceElement.optionEnumType(),
                        enumConstant);
            }
            code.addStatement("$N.put($T.$L, new $T())",
                    commonFields.optionParsers(), sourceElement.optionEnumType(),
                    enumConstant, optionParserType(namedOption));
        }
        return MethodSpec.constructorBuilder()
                .addCode(code.build())
                .build();
    }

    private ClassName optionParserType(Mapping<AnnotatedOption> param) {
        if (param.isRepeatable()) {
            return ClassName.get(OptionStateRepeatable.class);
        }
        if (param.modeFlag()) {
            return ClassName.get(OptionStateModeFlag.class);
        }
        return ClassName.get(OptionStateRegular.class);
    }
}
