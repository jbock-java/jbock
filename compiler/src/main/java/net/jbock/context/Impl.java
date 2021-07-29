package net.jbock.context;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceMethod;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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
    private final List<Mapping<?>> everything;

    @Inject
    Impl(GeneratedTypes generatedTypes,
         SourceElement sourceElement,
         List<Mapping<?>> everything) {
        this.generatedTypes = generatedTypes;
        this.sourceElement = sourceElement;
        this.everything = everything;
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
        if (sourceElement.isInterface()) {
            spec.addSuperinterface(sourceElement.typeName());
        } else {
            spec.superclass(sourceElement.typeName());
        }
        for (Mapping<?> c : everything) {
            spec.addField(c.asField());
        }
        return spec.addModifiers(PRIVATE, STATIC)
                .addMethod(implConstructor())
                .addMethods(everything.stream()
                        .map(this::parameterMethodOverride)
                        .collect(Collectors.toUnmodifiableList()))
                .build();
    }

    private MethodSpec parameterMethodOverride(Mapping<?> c) {
        SourceMethod<?> param = c.item();
        return MethodSpec.methodBuilder(param.methodName())
                .returns(TypeName.get(param.returnType()))
                .addModifiers(param.annotatedMethod().accessModifiers())
                .addStatement("return $N", c.asField())
                .build();
    }

    private MethodSpec implConstructor() {
        MethodSpec.Builder spec = MethodSpec.constructorBuilder();
        for (Mapping<?> c : everything) {
            FieldSpec field = c.asField();
            spec.addStatement("this.$N = $N", field, c.asParam());
            spec.addParameter(c.asParam());
        }
        return spec.build();
    }
}
