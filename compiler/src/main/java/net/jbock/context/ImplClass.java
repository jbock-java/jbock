package net.jbock.context;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;

/**
 * Defines the *_Impl class, which extends the command class.
 *
 * @see ParserClass
 */
@ContextScope
public class ImplClass {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final List<Mapping<?>> allMappings;
    private final GeneratedAnnotation generatedAnnotation;

    @Inject
    ImplClass(GeneratedTypes generatedTypes,
              SourceElement sourceElement,
              List<Mapping<?>> allMappings,
              GeneratedAnnotation generatedAnnotation) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.allMappings = allMappings;
        this.generatedAnnotation = generatedAnnotation;
    }

    public TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
        if (sourceElement.isInterface()) {
            spec.addSuperinterface(sourceElement.typeName());
        } else {
            spec.superclass(sourceElement.typeName());
        }
        return spec.addModifiers(FINAL)
                .addOriginatingElement(sourceElement.element())
                .addMethod(implConstructor())
                .addAnnotation(generatedAnnotation.define())
                .addFields(allMappings.stream()
                        .map(Mapping::asField)
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
                .addStatement("return $N", m.asField())
                .addAnnotation(Override.class)
                .build();
    }

    private MethodSpec implConstructor() {
        MethodSpec.Builder spec = MethodSpec.constructorBuilder();
        for (Mapping<?> m : allMappings) {
            FieldSpec field = m.asField();
            spec.addStatement("this.$N = $N", field, m.asParam());
            spec.addParameter(m.asParam());
        }
        return spec.build();
    }
}