package net.jbock.writing;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import io.jbock.simple.Inject;

import javax.lang.model.element.Modifier;

/**
 * Generates the *Parser class.
 */
@Inject
public record ParserClass(
        ParseMethod parseMethod,
        CommandRepresentation commandRepresentation,
        ParseOrExitMethod parseOrExitMethod,
        CreateImplMethod createImplMethod,
        CreateModelMethod createModelMethod,
        GeneratedAnnotation generatedAnnotation,
        ImplClass implClass) implements HasCommandRepresentation {

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

        spec.addMethod(createImplMethod.define());
        spec.addMethod(createModelMethod.get());
        Modifier[] modifiers = sourceElement().accessModifiers().toArray(new Modifier[0]);
        spec.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE)
                .build());

        return spec.addOriginatingElement(sourceElement().element())
                .addModifiers(modifiers)
                .addModifiers(Modifier.FINAL)
                .addType(implClass.define())
                .addAnnotation(generatedAnnotation.define()).build();
    }
}
