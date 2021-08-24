package net.jbock.context;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.state.OptionParser;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;

class CommonFields {

    private final FieldSpec optionNames;
    private final FieldSpec params;
    private final FieldSpec optionParsers;

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
            SourceElement sourceElement,
            List<Mapping<AnnotatedParameter>> positionalParameters,
            List<Mapping<AnnotatedOption>> namedOptions) {
        long mapSize = namedOptions.stream()
                .map(Mapping::sourceMethod)
                .map(AnnotatedOption::names)
                .map(List::size)
                .mapToLong(i -> i)
                .sum();
        FieldSpec optionsByName = FieldSpec.builder(mapOf(STRING, sourceElement.optionEnumType()), "optionNames")
                .initializer("new $T<>($L)", HashMap.class, mapSize)
                .build();
        FieldSpec paramParsers = FieldSpec.builder(ArrayTypeName.of(STRING), "params")
                .initializer("new $T[$L]", STRING, positionalParameters.size())
                .build();
        FieldSpec optionParsers = FieldSpec.builder(mapOf(sourceElement.optionEnumType(), ClassName.get(OptionParser.class)), "optionParsers")
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
}
