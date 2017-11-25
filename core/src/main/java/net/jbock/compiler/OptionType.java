package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;

import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Parser.OptionType enum.
 *
 * @see Parser
 */
final class OptionType {

  final ClassName type;

  private final FieldSpec isSpecialField;
  private final FieldSpec isBindingField;

  final MethodSpec isSpecialMethod;
  final MethodSpec isBindingMethod;

  private OptionType(Context context) {
    this.isSpecialField = FieldSpec.builder(TypeName.BOOLEAN, "special", PRIVATE, FINAL)
        .build();
    this.isBindingField = FieldSpec.builder(TypeName.BOOLEAN, "binding", PRIVATE, FINAL)
        .build();
    this.type = context.generatedClass.nestedClass("OptionType");
    this.isSpecialMethod = isSpecialMethod(isSpecialField);
    this.isBindingMethod = isBindingMethod(isBindingField);
  }

  static OptionType create(Context context) {
    return new OptionType(context);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (Type optionType : Type.values()) {
      builder.addEnumConstant(optionType.name(),
          anonymousClassBuilder("$L, $L", optionType.special, optionType.binding)
              .build());
    }
    return builder.addModifiers(PUBLIC)
        .addField(isSpecialField)
        .addField(isBindingField)
        .addMethod(privateConstructor())
        .addMethod(isSpecialMethod)
        .addMethod(isBindingMethod)
        .build();
  }

  private MethodSpec privateConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    ParameterSpec special = ParameterSpec.builder(isSpecialField.type, isSpecialField.name).build();
    ParameterSpec binding = ParameterSpec.builder(isBindingField.type, isBindingField.name).build();
    builder.addStatement("this.$N = $N", isSpecialField, special);
    builder.addStatement("this.$N = $N", isBindingField, binding);
    return builder.addParameter(special).addParameter(binding).build();
  }

  private static MethodSpec isSpecialMethod(FieldSpec isSpecialField) {
    return MethodSpec.methodBuilder("isSpecial")
        .addStatement("return $N", isSpecialField)
        .returns(TypeName.BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec isBindingMethod(FieldSpec isBindingField) {
    return MethodSpec.methodBuilder("isBinding")
        .addStatement("return $N", isBindingField)
        .returns(TypeName.BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }
}
