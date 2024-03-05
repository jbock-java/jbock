package net.jbock.writing;

import io.jbock.javapoet.AnnotationSpec;
import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;

import javax.lang.model.element.Modifier;

/**
 * Generates the *Parser class.
 */
final class ParserClass extends HasCommandRepresentation {

    private final ParseMethod parseMethod;
    private final OptEnum optionEnum;
    private final ParseOrExitMethod parseOrExitMethod;
    private final CreateModelMethod createModelMethod;
    private final GeneratedAnnotation generatedAnnotation;
    private final OptionNamesMethod optionNamesMethod;
    private final OptionStatesMethod optionStatesMethod;
    private final ImplClass implClass;

    @Inject
    ParserClass(
            ParseMethod parseMethod,
            CommandRepresentation commandRepresentation,
            OptEnum optionEnum,
            ParseOrExitMethod parseOrExitMethod,
            CreateModelMethod createModelMethod,
            GeneratedAnnotation generatedAnnotation,
            OptionNamesMethod optionNamesMethod,
            OptionStatesMethod optionStatesMethod,
            ImplClass implClass) {
        super(commandRepresentation);
        this.parseMethod = parseMethod;
        this.optionEnum = optionEnum;
        this.parseOrExitMethod = parseOrExitMethod;
        this.createModelMethod = createModelMethod;
        this.generatedAnnotation = generatedAnnotation;
        this.optionNamesMethod = optionNamesMethod;
        this.optionStatesMethod = optionStatesMethod;
        this.implClass = implClass;
    }

    /**
     * Entry point for code generation.
     *
     * @return type spec of the generated {@code *Parser}
     */
    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(sourceElement().generatedClass());
        spec.addMethod(parseMethod.get());
        if (!sourceElement().skipGeneratingParseOrExitMethod()) {
            spec.addMethod(parseOrExitMethod.define());
        }
        if (!namedOptions().isEmpty()) {
            spec.addMethod(optionNamesMethod.get());
            spec.addMethod(optionStatesMethod.get());
            spec.addType(optionEnum.define());
        }

        spec.addMethod(createModelMethod.get());
        Modifier[] modifiers = sourceElement().accessModifiers().toArray(new Modifier[0]);
        spec.addMethod(MethodSpec.constructorBuilder().addModifiers(modifiers)
                .addJavadoc("Constructor is deprecated, use the static methods instead.")
                .addAnnotation(AnnotationSpec.builder(Deprecated.class).addMember("forRemoval", "true").build())
                .build());

        return spec.addOriginatingElement(sourceElement().element())
                .addModifiers(modifiers)
                .addModifiers(Modifier.FINAL)
                .addType(implClass.define())
                .addAnnotation(generatedAnnotation.define()).build();
    }
}
