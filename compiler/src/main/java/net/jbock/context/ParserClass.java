package net.jbock.context;

import com.squareup.javapoet.TypeSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Generates the *Parser class.
 */
@ContextScope
public final class ParserClass {

    private final ParseMethod parseMethod;
    private final OptEnum optionEnum;
    private final StatefulParser statefulParser;
    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final ParseOrExitMethod parseOrExitMethod;
    private final CreateModelMethod createModelMethod;
    private final MultilineConverter multilineConverter;
    private final List<Mapping<?>> allMappings;
    private final GeneratedAnnotation generatedAnnotation;
    private final ConstructMethod constructMethod;

    @Inject
    ParserClass(
            ParseMethod parseMethod,
            SourceElement sourceElement,
            OptEnum optionEnum,
            StatefulParser statefulParser,
            List<Mapping<AnnotatedOption>> namedOptions,
            ParseOrExitMethod parseOrExitMethod,
            CreateModelMethod createModelMethod,
            MultilineConverter multilineConverter,
            List<Mapping<?>> allMappings,
            GeneratedAnnotation generatedAnnotation,
            ConstructMethod constructMethod) {
        this.parseMethod = parseMethod;
        this.sourceElement = sourceElement;
        this.optionEnum = optionEnum;
        this.statefulParser = statefulParser;
        this.namedOptions = namedOptions;
        this.parseOrExitMethod = parseOrExitMethod;
        this.createModelMethod = createModelMethod;
        this.multilineConverter = multilineConverter;
        this.allMappings = allMappings;
        this.generatedAnnotation = generatedAnnotation;
        this.constructMethod = constructMethod;
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
        spec.addMethod(constructMethod.get());

        spec.addType(statefulParser.define());
        if (!namedOptions.isEmpty()) {
            spec.addType(optionEnum.define());
        }

        for (Mapping<?> item : allMappings) {
            item.multilineBlock().ifPresent(multilineBlock ->
                    spec.addType(multilineConverter.define(item, multilineBlock)));
        }

        spec.addMethod(createModelMethod.get());

        return spec.addOriginatingElement(sourceElement.element())
                .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
                .addModifiers(Modifier.FINAL)
                .addAnnotation(generatedAnnotation.define()).build();
    }
}
