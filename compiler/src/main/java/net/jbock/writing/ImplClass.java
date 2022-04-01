package net.jbock.writing;

import io.jbock.javapoet.MethodSpec;
import io.jbock.javapoet.TypeName;
import io.jbock.javapoet.TypeSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *_Impl class, which extends the command class.
 *
 * @see ParserClass
 */
@WritingScope
public class ImplClass {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final List<Mapping<?>> allMappings;

    @Inject
    ImplClass(GeneratedTypes generatedTypes,
              CommandRepresentation commandRepresentation) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = commandRepresentation.sourceElement();
        this.allMappings = commandRepresentation.allMappings();
    }

    public TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
        if (sourceElement.isInterface()) {
            spec.addSuperinterface(sourceElement.typeName());
        } else {
            spec.superclass(sourceElement.typeName());
        }
        return spec.addModifiers(PRIVATE, STATIC, FINAL)
                .addFields(allMappings.stream()
                        .map(Mapping::field)
                        .collect(toList()))
                .addMethods(allMappings.stream()
                        .map(this::parameterMethodOverride)
                        .collect(toList()))
                .build();
    }

    private MethodSpec parameterMethodOverride(Mapping<?> m) {
        AnnotatedMethod sourceMethod = m.sourceMethod();
        return MethodSpec.methodBuilder(sourceMethod.methodName())
                .returns(TypeName.get(sourceMethod.returnType()))
                .addModifiers(sourceMethod.accessModifiers())
                .addStatement("return $N", m.field())
                .addAnnotation(Override.class)
                .build();
    }
}
