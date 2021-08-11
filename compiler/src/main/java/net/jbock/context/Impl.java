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
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Defines the *Impl inner class.
 *
 * @see GeneratedClass
 */
@ContextScope
public class Impl {

    private final GeneratedTypes generatedTypes;
    private final SourceElement sourceElement;
    private final List<Mapping<?>> allMappings;

    @Inject
    Impl(GeneratedTypes generatedTypes,
         SourceElement sourceElement,
         List<Mapping<?>> allMappings) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.allMappings = allMappings;
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
        if (sourceElement.isInterface()) {
            spec.addSuperinterface(sourceElement.typeName());
        } else {
            spec.superclass(sourceElement.typeName());
        }
        for (Mapping<?> m : allMappings) {
            spec.addField(m.asField());
        }
        return spec.addModifiers(PRIVATE, STATIC)
                .addMethod(implConstructor())
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
