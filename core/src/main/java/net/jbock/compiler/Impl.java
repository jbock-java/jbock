package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import java.util.ArrayList;
import java.util.List;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Defines the *_Impl inner class.
 *
 * @see Parser
 */
final class Impl {

  final ClassName type;

  final MethodSpec createMethod;

  private final Option option;

  private final Context context;

  private final List<FieldSpec> fields;

  private Impl(
      ClassName type,
      MethodSpec createMethod,
      Option option,
      Context context,
      List<FieldSpec> fields) {
    this.type = type;
    this.createMethod = createMethod;
    this.option = option;
    this.context = context;
    this.fields = fields;
  }

  static Impl create(
      Context context,
      ClassName implType,
      OptionType optionType,
      Option option) {
    List<FieldSpec> fields = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      fields.add(FieldSpec.builder(param.paramType.returnType, param.methodName())
          .addModifiers(PRIVATE, FINAL)
          .build());
    }
    MethodSpec createMethod = createMethod(context, implType, option, optionType);
    return new Impl(
        implType,
        createMethod,
        option,
        context,
        fields);
  }

  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type);
    builder.superclass(TypeName.get(context.sourceType.asType()))
        .addFields(fields)
        .addModifiers(PRIVATE, STATIC, FINAL)
        .addMethod(implConstructor())
        .addMethod(createMethod)
        .addMethods(bindMethods());
    if (context.paramTypes.contains(Type.REQUIRED)) {
      builder.addMethod(option.extractRequiredMethod);
    }
    if (context.paramTypes.contains(Type.REQUIRED_INT)) {
      builder.addMethod(option.extractRequiredIntMethod);
    }
    if (context.paramTypes.contains(Type.OPTIONAL_INT)) {
      builder.addMethod(option.extractOptionalIntMethod);
    }
    if (context.paramTypes.contains(Type.POSITIONAL_LIST)) {
      builder.addMethod(option.extractPositionalListMethod);
    }
    if (context.paramTypes.contains(Type.POSITIONAL_OPTIONAL)) {
      builder.addMethod(option.extractPositionalOptionalMethod);
    }
    if (context.paramTypes.contains(Type.POSITIONAL_REQUIRED)) {
      builder.addMethod(option.extractPositionalRequiredMethod);
    }
    if (context.paramTypes.contains(Type.POSITIONAL_LIST_2)) {
      builder.addMethod(option.extractPositionalList2Method);
    }
    return builder.build();
  }

  private static MethodSpec createMethod(
      Context context,
      ClassName implType,
      Option option,
      OptionType optionType) {
    CodeBlock.Builder args = CodeBlock.builder().add("\n    ");
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      args.add(param.paramType.extractExpression(option, j));
      if (j < context.parameters.size() - 1) {
        args.add(",\n    ");
      }
    }
    MethodSpec.Builder builder = MethodSpec.methodBuilder("create");
    builder.addParameter(option.optMapParameter);
    builder.addParameter(option.sMapParameter);
    builder.addParameter(option.flagsParameter);
    builder.addParameter(option.positionalParameter);
    builder.addParameter(option.ddIndexParameter);
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();

    builder.addStatement("return new $T($L)", implType, args.build());
    return builder.addModifiers(STATIC)
        .returns(implType)
        .build();
  }

  private List<MethodSpec> bindMethods() {
    List<MethodSpec> result = new ArrayList<>(context.parameters.size());
    for (int j = 0; j < context.parameters.size(); j++) {
      Param param = context.parameters.get(j);
      MethodSpec.Builder builder = MethodSpec.methodBuilder(param.methodName())
          .addModifiers(PUBLIC)
          .addAnnotation(Override.class)
          .returns(param.paramType.returnType)
          .addStatement("return $N", fields.get(j));
      result.add(builder.build());
    }
    return result;
  }

  private MethodSpec implConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder();
    for (FieldSpec field : fields) {
      ParameterSpec param = ParameterSpec.builder(field.type, field.name).build();
      if (field.type.isPrimitive()) {
        builder.addStatement("this.$N = $N", field, param);
      } else {
        builder.addStatement("this.$N = requireNonNull($N)", field, param);
      }
      builder.addParameter(param);
    }
    return builder.build();
  }
}
