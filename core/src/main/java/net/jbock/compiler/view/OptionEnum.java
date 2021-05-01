package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.Parameter;

import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.coerce.Util.arraysOfStringInvocation;
import static net.jbock.compiler.Constants.LIST_OF_STRING;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see GeneratedClass
 */
final class OptionEnum {

  private final Context context;

  private final GeneratedTypes generatedTypes;

  private final FieldSpec descriptionField;

  @Inject
  OptionEnum(Context context, GeneratedTypes generatedTypes) {
    this.context = context;
    this.generatedTypes = generatedTypes;
    this.descriptionField = FieldSpec.builder(LIST_OF_STRING, "description").build();
  }

  TypeSpec define() {
    List<Coercion<? extends Parameter>> parameters = context.parameters();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(generatedTypes.optionType());
    for (Coercion<? extends Parameter> param : parameters) {
      String enumConstant = param.enumConstant();
      spec.addEnumConstant(enumConstant, optionEnumConstant(param));
    }
    return spec.addModifiers(PRIVATE)
        .addField(descriptionField)
        .addMethod(privateConstructor())
        .build();
  }

  private TypeSpec optionEnumConstant(Coercion<? extends Parameter> c) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("descExpression", descExpression(c.parameter().description()));
    String format = "$descExpression:L";

    return anonymousClassBuilder(CodeBlock.builder().addNamed(format, map).build()).build();
  }

  private CodeBlock descExpression(List<String> desc) {
    switch (desc.size()) {
      case 0:
        return CodeBlock.builder().add("$T.emptyList()", Collections.class).build();
      case 1:
        return CodeBlock.builder().add("$T.singletonList($S)", Collections.class, desc.get(0)).build();
      default:
        return arraysOfStringInvocation(desc);
    }
  }

  private MethodSpec privateConstructor() {
    ParameterSpec description = builder(descriptionField.type, descriptionField.name).build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", descriptionField, description)
        .addParameter(description)
        .build();
  }
}
