package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.util.NotSuccess;
import net.jbock.util.SuperResult;

import javax.inject.Inject;
import java.util.Optional;

import static net.jbock.common.Constants.EITHER;

@ContextScope
public class GeneratedTypes {

    private final SourceElement sourceElement;
    private final ClassName generatedClass;

    @Inject
    GeneratedTypes(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
        this.generatedClass = sourceElement.generatedClass();
    }

    TypeName parseSuccessType() {
        return superResultType().orElse(sourceElement.typeName());
    }

    Optional<TypeName> superResultType() {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        ParameterizedTypeName type = ParameterizedTypeName.get(
                ClassName.get(SuperResult.class),
                sourceElement.typeName());
        return Optional.of(type);
    }

    ClassName optionParserType() {
        return generatedClass.nestedClass("OptionParser");
    }

    ClassName repeatableOptionParserType() {
        return generatedClass.nestedClass("RepeatableOptionParser");
    }

    ClassName flagParserType() {
        return generatedClass.nestedClass("FlagParser");
    }

    ClassName regularOptionParserType() {
        return generatedClass.nestedClass("RegularOptionParser");
    }

    ClassName statefulParserType() {
        return generatedClass.nestedClass("StatefulParser");
    }

    ClassName implType() {
        return generatedClass.nestedClass(sourceElement.element().getSimpleName() + "Impl");
    }

    ClassName multilineConverterType(Mapping<?> item) {
        String name = item.enumName().original();
        String capitalized = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        return generatedClass.nestedClass(capitalized + "Converter");
    }

    TypeName parseResultType() {
        return ParameterizedTypeName.get(
                EITHER,
                ClassName.get(NotSuccess.class),
                parseSuccessType());
    }
}
