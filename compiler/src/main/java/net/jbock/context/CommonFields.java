package net.jbock.context;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.util.HelpRequested;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;

class CommonFields {

    private final FieldSpec optionNames;
    private final FieldSpec params;
    private final FieldSpec optionParsers;

    private final FieldSpec values = FieldSpec.builder(LIST_OF_STRING, "values")
            .build();
    private final FieldSpec value = FieldSpec.builder(STRING, "value")
            .build();
    private final FieldSpec seen = FieldSpec.builder(BOOLEAN, "seen")
            .build();


    private final FieldSpec rest = FieldSpec.builder(LIST_OF_STRING, "rest")
            .initializer("new $T<>()", ArrayList.class)
            .build();

    private final FieldSpec suspiciousPattern = FieldSpec.builder(Pattern.class, "sus")
            .initializer("$T.compile($S)", Pattern.class, "-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+")
            .build();

    private CommonFields(
            FieldSpec optionNames,
            FieldSpec params,
            FieldSpec optionParsers) {
        this.optionNames = optionNames;
        this.params = params;
        this.optionParsers = optionParsers;
    }

    static CommonFields create(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            NamedOptions namedOptions) {
        ParameterSpec result = ParameterSpec.builder(generatedTypes.parseResultType(), "result").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.add(CodeBlock.builder()
                .add("$N ->\n", result).indent()
                .add("$T.exit($N instanceof $T ? 0 : 1)", System.class, result, HelpRequested.class)
                .unindent().build());
        long mapSize = namedOptions.stream()
                .map(Mapping::item)
                .map(t -> t.annotatedMethod().names())
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        FieldSpec optionsByName = FieldSpec.builder(mapOf(STRING, sourceElement.optionEnumType()), "optionNames")
                .initializer("new $T<>($L)", HashMap.class, mapSize)
                .build();
        FieldSpec paramParsers = FieldSpec.builder(ArrayTypeName.of(STRING), "params")
                .initializer("new $T[$L]", STRING, positionalParameters.size())
                .build();
        FieldSpec optionParsers = FieldSpec.builder(mapOf(sourceElement.optionEnumType(), generatedTypes.optionParserType()), "optionParsers")
                .initializer("new $T<>($T.class)", EnumMap.class, sourceElement.optionEnumType())
                .build();
        return new CommonFields(optionsByName, paramParsers, optionParsers);
    }

    FieldSpec suspiciousPattern() {
        return suspiciousPattern;
    }

    FieldSpec optionNames() {
        return optionNames;
    }

    FieldSpec rest() {
        return rest;
    }

    FieldSpec params() {
        return params;
    }

    FieldSpec optionParsers() {
        return optionParsers;
    }

    FieldSpec values() {
        return values;
    }

    FieldSpec value() {
        return value;
    }

    FieldSpec seen() {
        return seen;
    }
}
