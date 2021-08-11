package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.convert.Mapping;
import net.jbock.util.StringConverter;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class MultilineConverter {

    @Inject
    MultilineConverter() {
    }

    TypeSpec define(Mapping<?> m, CodeBlock multilineBlock) {
        return TypeSpec.classBuilder(m.multilineConverterType())
                .addMethod(convertMethod(m, multilineBlock))
                .superclass(ParameterizedTypeName.get(
                        ClassName.get(StringConverter.class),
                        TypeName.get(m.baseType())))
                .addModifiers(PRIVATE, STATIC)
                .build();
    }

    private MethodSpec convertMethod(Mapping<?> m, CodeBlock multilineBlock) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("convert");
        spec.addAnnotation(Override.class);
        spec.addCode(multilineBlock);
        spec.addParameter(ParameterSpec.builder(STRING, "token").build());
        spec.addModifiers(PROTECTED);
        spec.returns(TypeName.get(m.baseType()));
        return spec.build();
    }
}
