package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.coerce.Coercion;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.compiler.parameter.Parameter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;

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
    this.descriptionField = FieldSpec.builder(STRING_ARRAY, "description").build();
  }

  TypeSpec define() {
    List<Coercion<? extends Parameter>> parameters = context.parameters();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(generatedTypes.optionType());
    for (Coercion<? extends Parameter> param : parameters) {
      String enumConstant = param.enumConstant();
      List<String> description = param.parameter().description();
      TypeSpec optionSpec = anonymousClassBuilder(descExpression(description)).build();
      spec.addEnumConstant(enumConstant, optionSpec);
    }
    return spec.addModifiers(PRIVATE)
        .addField(descriptionField)
        .addMethod(privateConstructor())
        .build();
  }

  private CodeBlock descExpression(List<String> desc) {
    CodeBlock.Builder code = CodeBlock.builder();
    for (int i = 0; i < desc.size(); i++) {
      code.add("$S", desc.get(i));
      if (i != desc.size() - 1) {
        code.add(",\n");
      }
    }
    return code.build();

  }

  private MethodSpec privateConstructor() {
    ParameterSpec description = builder(ArrayTypeName.of(String.class), "description").build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", descriptionField, description)
        .addParameter(description)
        .varargs(true)
        .build();
  }
}
