package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.convert.Mapping;
import net.jbock.convert.matching.MapExpr;
import net.jbock.util.StringConverter;

import javax.inject.Inject;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class MultilineConverter {

    private final GeneratedTypes generatedTypes;

    @Inject
    MultilineConverter(GeneratedTypes generatedTypes) {
        this.generatedTypes = generatedTypes;
    }

    TypeSpec define(Mapping<?> item) {
        MapExpr mapExpr = item.mapExpr();
        return TypeSpec.classBuilder(generatedTypes.multilineConverterType(item))
                .addMethod(convertMethod(mapExpr))
                .superclass(ParameterizedTypeName.get(
                        ClassName.get(StringConverter.class),
                        TypeName.get(mapExpr.type())))
                .addModifiers(PRIVATE, STATIC)
                .build();
    }

    private MethodSpec convertMethod(MapExpr mapExpr) {
        MethodSpec.Builder spec = MethodSpec.methodBuilder("convert");
        spec.addAnnotation(Override.class);
        spec.addCode(mapExpr.code());
        spec.addParameter(ParameterSpec.builder(STRING, "token").build());
        spec.addModifiers(PROTECTED);
        spec.returns(TypeName.get(mapExpr.type()));
        return spec.build();
    }
}
