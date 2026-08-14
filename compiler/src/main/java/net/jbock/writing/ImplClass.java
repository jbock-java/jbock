package net.jbock.writing;

import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.convert.Mapping;

import java.util.StringJoiner;

import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Implementation of the command interface.
 */
final class ImplClass extends HasCommandRepresentation {

    private final GeneratedTypes generatedTypes;

    @Inject
    ImplClass(GeneratedTypes generatedTypes,
              CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
        this.generatedTypes = generatedTypes;
    }

    TypeSpec define() {
        if (sourceElement().isInterface()) {
            TypeSpec.Builder spec = TypeSpec.recordBuilder(generatedTypes.implType());
            spec.addModifiers(PRIVATE, STATIC);
            spec.recordConstructor(recordConstructor());
            spec.addSuperinterface(sourceElement().typeName());
            return spec.build();
        } else {
            TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.implType());
            spec.superclass(sourceElement().typeName());
            return spec.addModifiers(PRIVATE, STATIC, FINAL)
                    .addMethod(allArgsConstructor())
                    .addMethod(generateToString())
                    .addFields(allMappings().stream()
                            .map(Mapping::field)
                            .toList())
                    .addMethods(allMappings().stream()
                            .map(this::parameterMethodOverride)
                            .collect(toList()))
                    .build();
        }
    }

    private MethodSpec parameterMethodOverride(Mapping<?> m) {
        Item sourceMethod = m.item();
        return MethodSpec.methodBuilder(sourceMethod.methodName())
                .returns(TypeName.get(sourceMethod.returnType()))
                .addModifiers(sourceMethod.accessModifiers())
                .addStatement("return $N", m.field())
                .addAnnotation(Override.class)
                .build();
    }

    private MethodSpec allArgsConstructor() {
        MethodSpec.Builder spec = MethodSpec.constructorBuilder();
        for (int i = 0; i < namedOptions().size(); i++) {
            Mapping<Option> m = namedOptions().get(i);
            spec.addStatement("this.$N = $N", m.field(), m.param());
            spec.addParameter(m.param());
        }
        for (int i = 0; i < positionalParameters().size(); i++) {
            Mapping<Parameter> m = positionalParameters().get(i);
            spec.addStatement("this.$N = $N", m.field(), m.param());
            spec.addParameter(m.param());
        }
        varargsParameter().ifPresent(m -> {
            spec.addStatement("this.$N = $N", m.field(), m.param());
            spec.addParameter(m.param());
        });
        return spec.build();
    }

    private MethodSpec recordConstructor() {
        MethodSpec.Builder spec = MethodSpec.constructorBuilder();
        for (int i = 0; i < namedOptions().size(); i++) {
            spec.addParameter(namedOptions().get(i).param());
        }
        for (int i = 0; i < positionalParameters().size(); i++) {
            spec.addParameter(positionalParameters().get(i).param());
        }
        varargsParameter().ifPresent(m -> spec.addParameter(m.param()));
        return spec.build();
    }

    private MethodSpec generateToString() {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("toString").addModifiers(PUBLIC);
        spec.addAnnotation(Override.class);
        ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();
        spec.addStatement("$T $N = new $T($S, $S, $S)", StringJoiner.class, joiner, StringJoiner.class,
                ", ", "{ ", " }");
        for (int i = 0; i < namedOptions().size(); i++) {
            Mapping<Option> m = namedOptions().get(i);
            spec.addStatement("$N.add($S + $N)", joiner, m.field().name() + ": ", m.field());
        }
        for (int i = 0; i < positionalParameters().size(); i++) {
            Mapping<Parameter> m = positionalParameters().get(i);
            spec.addStatement("$N.add($S + $N)", joiner, m.field().name() + ": ", m.field());
        }
        spec.addStatement("return $N.toString()", joiner);
        return spec.returns(String.class).build();
    }
}
