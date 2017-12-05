package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;

import java.util.Arrays;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Parser.OptionType enum.
 *
 * @see Parser
 */
final class OptionType {

  final ClassName type;

  private final Context context;

  final FieldSpec isSpecialField;
  final FieldSpec isBindingField;
  final FieldSpec isRequiredField;

  private OptionType(Context context) {
    this.context = context;
    this.isSpecialField = FieldSpec.builder(BOOLEAN, "special", PRIVATE, FINAL)
        .build();
    this.isBindingField = FieldSpec.builder(BOOLEAN, "binding", PRIVATE, FINAL)
        .build();
    this.isRequiredField = FieldSpec.builder(BOOLEAN, "required", PRIVATE, FINAL)
        .build();
    this.type = context.generatedClass.nestedClass("OptionType");
  }

  static OptionType create(Context context) {
    return new OptionType(context);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (Type optionType : context.paramTypes) {
      addType(builder, optionType);
    }
    return builder.addModifiers(PUBLIC)
        .addField(isSpecialField)
        .addField(isBindingField)
        .addField(isRequiredField)
        .addMethod(privateConstructor())
        .build();
  }

  private void addType(TypeSpec.Builder builder, Type optionType) {
    builder.addEnumConstant(optionType.name(),
        anonymousClassBuilder("$L, $L, $L",
            optionType.special, optionType.binding, optionType.required)
            .build());
  }

  private MethodSpec privateConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    ParameterSpec special = ParameterSpec.builder(BOOLEAN, isSpecialField.name).build();
    ParameterSpec binding = ParameterSpec.builder(BOOLEAN, isBindingField.name).build();
    ParameterSpec required = ParameterSpec.builder(BOOLEAN, isRequiredField.name).build();
    builder.addStatement("this.$N = $N", isSpecialField, special);
    builder.addStatement("this.$N = $N", isBindingField, binding);
    builder.addStatement("this.$N = $N", isRequiredField, required);
    return builder.addParameters(Arrays.asList(special, binding, required)).build();
  }
}
