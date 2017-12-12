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
  private final OptionType optionType;
  private final Option option;
  private final Helper helper;
  private final Impl impl;

  private Parser(
      Context context,
      OptionType optionType,
      Option option,
      Helper helper,
      Impl impl) {
    this.context = context;
    this.optionType = optionType;
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
    Helper helper = Helper.create(context, impl, optionType, option);
    return new Parser(context, optionType, option, helper, impl);
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(context.generatedClass)
        .addType(helper.define())
        .addType(option.define())
        .addType(impl.define())
        .addType(optionType.define())
        .addMethod(parseMethod())
        .addMethod(printUsageMethod())
        .addMethod(privateConstructor())
        .addModifiers(PUBLIC, FINAL)
        .addJavadoc(javadoc())
        .build();
  }

  private MethodSpec parseMethod() {

    ParameterSpec helper = ParameterSpec.builder(this.helper.type, "helper").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder builder = CodeBlock.builder();

    builder.add("\n");
    builder.addStatement("$T $N = new $T()", helper.type, helper, helper.type);
    builder.addStatement("$T $N = $T.asList($N).iterator()", it.type, it, Arrays.class, args);

    if (context.stopword) {
      builder.add("\n");
      builder.addStatement("$T $N = $S", stopword.type, stopword, "--");
    }

    builder.add("\n");
    if (!context.positionalParameters.isEmpty()) {
      builder.addStatement("$T $N = new $T<>()",
          LIST_OF_STRING, this.helper.positionalParameter, ArrayList.class);
      builder.addStatement("$T $N = $L", INT, this.helper.ddIndexParameter, -1);
    }

    // Begin parsing loop
    builder.add("\n");
    builder.beginControlFlow("while ($N.hasNext())", it);

    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.stopword) {
      builder.beginControlFlow("if ($N.equals($N))", token, stopword)
          .addStatement("$N = $N.size()", this.helper.ddIndexParameter, this.helper.positionalParameter)
          .addStatement("$N.forEachRemaining($N::add)", it, this.helper.positionalParameter)
          .addStatement(buildExpression(helper))
          .endControlFlow();
    }


    // handle positional token
    builder.beginControlFlow("if ($N.length() <= 1 || $N.charAt(0) != '-')", token, token);
    if (!context.positionalParameters.isEmpty()) {
      builder.addStatement("$N.add($N)", this.helper.positionalParameter, token);
    } else {
      builder.addStatement("throw new $T($S + $N)",
          IllegalArgumentException.class, "Unknown token: ", token);
    }
    builder.endControlFlow();

    if (context.grouping) {
      builder.beginControlFlow("else if ($N.$N($N))", helper, this.helper.looksLikeGroupMethod, token)
          .addStatement("$N.$N($N)",
              helper, this.helper.readGroupMethod, token)
          .endControlFlow();
    }

    builder.beginControlFlow("else")
        .addStatement("$N.$N($N, $N)", helper, this.helper.readMethod, token, it)
        .endControlFlow();

    // End parsing loop
    builder.endControlFlow();

    builder.add("\n");
    builder.addStatement(buildExpression(helper));

    return MethodSpec.methodBuilder("parse")
        .addParameter(args)
        .addCode(builder.build())
        .returns(TypeName.get(context.sourceType.asType()))
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private CodeBlock buildExpression(ParameterSpec helper) {
    if (context.positionalParameters.isEmpty()) {
      return CodeBlock.builder()
          .add("return $N.build()", helper)
          .build();
    }
    return CodeBlock.builder()
        .add("return $N.build($N, $N)",
            helper, this.helper.positionalParameter, this.helper.ddIndexParameter)
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
