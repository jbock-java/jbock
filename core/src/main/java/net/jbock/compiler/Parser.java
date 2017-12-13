package net.jbock.compiler;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.INT;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

/**
 * Generates the *_Parser class.
 */
final class Parser {

  private final Context context;
  private final Option option;
  private final Helper helper;
  private final Impl impl;

  private Parser(
      Context context,
      Option option,
      Helper helper,
      Impl impl) {
    this.context = context;
    this.option = option;
    this.helper = helper;
    this.impl = impl;
  }

  static Parser create(Context context) {
    ClassName implType = context.generatedClass.nestedClass(
        context.sourceType.getSimpleName() + "Impl");
    OptionType optionType = OptionType.create(context);
    Option option = Option.create(context, optionType);
    Impl impl = Impl.create(context, implType);
    Helper helper = Helper.create(context, impl, option);
    return new Parser(context, option, helper, impl);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(context.generatedClass)
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(option.optionType.define())
        .addMethod(parseMethod())
        .addMethod(printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec helper = ParameterSpec.builder(this.helper.type, "helper").build();
    ParameterSpec tokens = ParameterSpec.builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec dd = ParameterSpec.builder(STRING, "dd").build();
    ParameterSpec optionParam = ParameterSpec.builder(option.type, "option").build();
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse")
        .addParameter(args)
        .returns(TypeName.get(context.sourceType.asType()))
        .addModifiers(PUBLIC, STATIC);

    builder.addStatement("$T $N = new $T()", helper.type, helper, helper.type);
    builder.addStatement("$T $N = $T.asList($N)", LIST_OF_STRING, tokens, Arrays.class, args);

    if (!context.stopword && context.paramTypes.isEmpty() && context.ignoreDashes) {
      return builder
          .addStatement("return $N.build($N, $L)", helper, tokens, -1)
          .build();
    }

    builder.addStatement("$T $N = $N.iterator()", STRING_ITERATOR, it, tokens);

    if (context.stopword) {
      builder.addStatement("$T $N = $S", dd.type, dd, "--");
    }

    if (!context.positionalParameters.isEmpty()) {
      builder.addStatement("$T $N = new $T<>()",
          LIST_OF_STRING, this.helper.positionalParameter, ArrayList.class);
    }

    // Begin parsing loop
    builder.beginControlFlow("while ($N.hasNext())", it);

    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.stopword) {

      builder.beginControlFlow("if ($N.equals($N))", dd, token)
          .addStatement("$T $N = $N.size()", INT, this.helper.ddIndexParameter, this.helper.positionalParameter)
          .addStatement("$N.forEachRemaining($N::add)", it, this.helper.positionalParameter)
          .addStatement(buildExpression(helper, CodeBlock.builder().add("$N", this.helper.ddIndexParameter).build()))
          .endControlFlow();
    }

    if (context.paramTypes.isEmpty()) {

      builder.addStatement("$N.add($N)", this.helper.positionalParameter, token);
    } else {

      // handle positional token
      builder.addStatement("$T $N = $N.$N($N)", option.type, optionParam, helper, this.helper.readRegularOptionMethod, token);
      builder.beginControlFlow("if ($N != null)", optionParam);
      builder.addStatement("$N.$N($N, $N, $N)", helper, this.helper.readMethod, optionParam, token, it);
      builder.endControlFlow();

      if (context.positionalParameters.isEmpty()) {

        builder.beginControlFlow("else")
            .addStatement("throw new $T($S + $N)",
                IllegalArgumentException.class, "Invalid option: ", token)
            .endControlFlow();

      } else {

        if (!context.ignoreDashes) {
          builder.beginControlFlow("else if ($N.length() >= 1 && $N.charAt(0) == '-')",
              token, token)
              .addStatement("throw new $T($S + $N)",
                  IllegalArgumentException.class, "Invalid option: ", token)
              .endControlFlow();
        }

        builder.beginControlFlow("else")
            .addStatement("$N.add($N)", this.helper.positionalParameter, token)
            .endControlFlow();
      }
    }

    // End parsing loop
    builder.endControlFlow();

    builder.addStatement(buildExpression(helper, CodeBlock.builder().add("$L", -1).build()));
    return builder.build();
  }

  private CodeBlock buildExpression(ParameterSpec helper, CodeBlock param) {
    if (context.positionalParameters.isEmpty()) {
      return CodeBlock.builder()
          .add("return $N.build()", helper)
          .build();
    }
    return CodeBlock.builder()
        .add("return $N.build($N, $L)",
            helper, this.helper.positionalParameter, param)
        .build();
  }

  private MethodSpec printUsageMethod() {
    ParameterSpec option = ParameterSpec.builder(this.option.type, "option").build();
    ParameterSpec out = ParameterSpec.builder(ClassName.get(PrintStream.class), "out").build();
    ParameterSpec indent = ParameterSpec.builder(INT, "indent").build();
    return MethodSpec.methodBuilder("printUsage")
        .beginControlFlow("for ($T $N: $T.values())", option.type, option, option.type)
        .addStatement("$N.println($N.describe($N))", out, option, indent)
        .endControlFlow()
        .addModifiers(STATIC, PUBLIC)
        .addParameters(Arrays.asList(out, indent))
        .build();
  }

  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by $L\n\n" +
            "@see <a href=\"https://github.com/h908714124/jbock\">jbock on github</a>\n",
        Processor.class.getName()).build();
  }

  private MethodSpec privateConstructor() {
    return MethodSpec.constructorBuilder()
        .addModifiers(PRIVATE)
        .build();
  }
}
