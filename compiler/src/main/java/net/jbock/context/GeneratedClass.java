package net.jbock.context;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.JbockProcessor;
import net.jbock.processor.SourceElement;

import javax.annotation.processing.Generated;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Generates the *Parser class.
 */
@ContextScope
public final class GeneratedClass {

    private final ParseMethod parseMethod;
    private final Impl impl;
    private final OptionParser optionParser;
    private final OptionEnum optionEnum;
    private final StatefulParser statefulParser;
    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final ParseOrExitMethod parseOrExitMethod;
    private final CreateModelMethod createModelMethod;
    private final MultilineConverter multilineConverter;
    private final List<Mapping<?>> everything;

    @Inject
    GeneratedClass(
            ParseMethod parseMethod,
            SourceElement sourceElement,
            Impl impl,
            OptionParser optionParser,
            OptionEnum optionEnum,
            StatefulParser statefulParser,
            List<Mapping<AnnotatedOption>> namedOptions,
            ParseOrExitMethod parseOrExitMethod,
            CreateModelMethod createModelMethod,
            MultilineConverter multilineConverter,
            List<Mapping<?>> everything) {
        this.parseMethod = parseMethod;
        this.sourceElement = sourceElement;
        this.impl = impl;
        this.optionParser = optionParser;
        this.optionEnum = optionEnum;
        this.statefulParser = statefulParser;
        this.namedOptions = namedOptions;
        this.parseOrExitMethod = parseOrExitMethod;
        this.createModelMethod = createModelMethod;
        this.multilineConverter = multilineConverter;
        this.everything = everything;
    }

    /**
     * Entry point for code generation.
     *
     * @return type spec of the generated {@code *Parser}
     */
    public TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass())
                .addMethod(parseMethod.get());
        if (sourceElement.generateParseOrExitMethod()) {
            spec.addMethod(parseOrExitMethod.define());
        }

        spec.addType(statefulParser.define());
        if (!namedOptions.isEmpty()) {
            spec.addType(optionEnum.define());
            spec.addTypes(optionParser.define());
        }
        spec.addType(impl.define());

        for (Mapping<?> item : everything) {
            if (item.multiline()) {
                spec.addType(multilineConverter.define(item));
            }
        }

        spec.addMethod(createModelMethod.get());

        return spec.addOriginatingElement(sourceElement.element()) // important
                .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
                .addAnnotation(generatedAnnotation()).build();
    }

    private AnnotationSpec generatedAnnotation() {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", CodeBlock.of("$S", JbockProcessor.class.getCanonicalName()))
                .addMember("comments", CodeBlock.of("$S", "https://github.com/jbock-java"))
                .build();
    }
}
