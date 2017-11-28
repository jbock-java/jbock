package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static net.jbock.compiler.Type.EVERYTHING_AFTER;
import static net.jbock.compiler.Type.OTHER_TOKENS;

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

  private final Context context;

  final FieldSpec isSpecialField;
  final FieldSpec isBindingField;

  private OptionType(Context context) {
    this.context = context;
    this.isSpecialField = FieldSpec.builder(TypeName.BOOLEAN, "special", PRIVATE, FINAL)
        .build();
    this.isBindingField = FieldSpec.builder(TypeName.BOOLEAN, "binding", PRIVATE, FINAL)
        .build();
    this.type = context.generatedClass.nestedClass("OptionType");
  }

  static OptionType create(Context context) {
    return new OptionType(context);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.enumBuilder(type);
    for (Type optionType : Type.values()) {
      if (!optionType.special) {
        addType(builder, optionType);
      }
    }
    if (context.otherTokens) {
      addType(builder, OTHER_TOKENS);
    }
    if (context.rest) {
      addType(builder, EVERYTHING_AFTER);
    }
    return builder.addModifiers(PUBLIC)
        .addField(isSpecialField)
        .addField(isBindingField)
        .addMethod(privateConstructor())
        .build();
  }

  private void addType(TypeSpec.Builder builder, Type optionType) {
    builder.addEnumConstant(optionType.name(),
        anonymousClassBuilder("$L, $L", optionType.special, optionType.binding)
            .build());
  }

  private MethodSpec privateConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    ParameterSpec special = ParameterSpec.builder(isSpecialField.type, isSpecialField.name).build();
    ParameterSpec binding = ParameterSpec.builder(isBindingField.type, isBindingField.name).build();
    builder.addStatement("this.$N = $N", isSpecialField, special);
    builder.addStatement("this.$N = $N", isBindingField, binding);
    return builder.addParameter(special).addParameter(binding).build();
  }
}
