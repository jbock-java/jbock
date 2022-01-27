package net.jbock.context;

import io.jbock.javapoet.TypeSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import javax.lang.model.element.Modifier;
import java.util.List;

/**
 * Generates the *Parser class.
 */
@ContextScope
public final class ParserClass {

    private final ParseMethod parseMethod;
    private final OptEnum optionEnum;
    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedOption>> namedOptions;
    private final ParseOrExitMethod parseOrExitMethod;
    private final CreateModelMethod createModelMethod;
    private final GeneratedAnnotation generatedAnnotation;
    private final HarvestMethod harvestMethod;
    private final OptionNamesMethod optionNamesMethod;
    private final OptionStatesMethod optionStatesMethod;
    private final CommonFields commonFields;

    @Inject
    ParserClass(
            ParseMethod parseMethod,
            SourceElement sourceElement,
            OptEnum optionEnum,
            List<Mapping<AnnotatedOption>> namedOptions,
            ParseOrExitMethod parseOrExitMethod,
            CreateModelMethod createModelMethod,
            GeneratedAnnotation generatedAnnotation,
            HarvestMethod harvestMethod,
            OptionNamesMethod optionNamesMethod,
            OptionStatesMethod optionStatesMethod,
            CommonFields commonFields) {
        this.parseMethod = parseMethod;
        this.sourceElement = sourceElement;
        this.optionEnum = optionEnum;
        this.namedOptions = namedOptions;
        this.parseOrExitMethod = parseOrExitMethod;
        this.createModelMethod = createModelMethod;
        this.generatedAnnotation = generatedAnnotation;
        this.harvestMethod = harvestMethod;
        this.optionNamesMethod = optionNamesMethod;
        this.optionStatesMethod = optionStatesMethod;
        this.commonFields = commonFields;
    }

    /**
     * Entry point for code generation.
     *
     * @return type spec of the generated {@code *Parser}
     */
    public TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement.generatedClass());
        spec.addMethod(parseMethod.get());
        if (!sourceElement.skipGeneratingParseOrExitMethod()) {
            spec.addMethod(parseOrExitMethod.define());
        }
        spec.addMethod(harvestMethod.get());
        if (!namedOptions.isEmpty()) {
            spec.addField(commonFields.optionNames().toBuilder()
                    .initializer("$N()", optionNamesMethod.get()).build());
            spec.addMethod(optionNamesMethod.get());
            spec.addMethod(optionStatesMethod.get());
            spec.addType(optionEnum.define());
        }

        spec.addMethod(createModelMethod.get());

        return spec.addOriginatingElement(sourceElement.element())
                .addModifiers(sourceElement.accessModifiers().toArray(new Modifier[0]))
                .addModifiers(Modifier.FINAL)
                .addAnnotation(generatedAnnotation.define()).build();
    }
}
