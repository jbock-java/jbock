package net.jbock.compiler;

import net.jbock.com.squareup.javapoet.ArrayTypeName;
import net.jbock.com.squareup.javapoet.ClassName;
import net.jbock.com.squareup.javapoet.CodeBlock;
import net.jbock.com.squareup.javapoet.FieldSpec;
import net.jbock.com.squareup.javapoet.MethodSpec;
import net.jbock.com.squareup.javapoet.ParameterSpec;
import net.jbock.com.squareup.javapoet.TypeName;
import net.jbock.com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Function;

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOf;
import static net.jbock.compiler.Util.optionalOfSubtype;

final class Tokenizer {

  final ClassName type;

  private final Context context;
  private final Option option;
  private final Helper helper;

  private final FieldSpec out;

  private Tokenizer(ClassName type, Context context, Option option, Helper helper, FieldSpec out) {
    this.out = out;
    this.type = type;
    this.context = context;
    this.option = option;
    this.helper = helper;
  }

  static Tokenizer create(Context context, Option option, Helper helper, IndentPrinter indentPrinter) {
    ClassName builderClass = context.generatedClass.nestedClass("Tokenizer");
    FieldSpec out = FieldSpec.builder(indentPrinter.type, "out")
        .addModifiers(FINAL).build();
    return new Tokenizer(builderClass, context, option, helper, out);
  }


  TypeSpec define() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(type)
        .addModifiers(STATIC, PRIVATE)
        .addMethod(parseMethod())
        .addMethod(parseListMethod())
        .addMethod(privateConstructor())
        .addMethod(printUsageMethod())
        .addMethod(synopsisMethod())
        .addMethod(printDescriptionMethod())
        .addField(out);
    return builder.build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = ParameterSpec.builder(ArrayTypeName.of(STRING), "args")
        .build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    builder.beginControlFlow("try")
        .addCode(parseMethodTryBlock(args))
        .endControlFlow();

    builder.beginControlFlow("catch ($T $N)", IllegalArgumentException.class, e)
        .addCode(parseMethodCatchBlock(e))
        .endControlFlow();

    return builder
        .addParameter(args)
        .returns(optionalOf(TypeName.get(context.sourceType.asType())))
        .build();
  }

  private MethodSpec printUsageMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("printUsage");

    // Name
    builder.addStatement("$N.println($S)", out, "NAME");
    builder.addStatement("$N.incrementIndent()", out);
    if (context.missionStatement.isEmpty()) {
      builder.addStatement("$N.println($S)", out, context.programName);
    } else {
      builder.addStatement("$N.println($T.format($S, $S, $S))",
          out, String.class, "%s - %s", context.programName, context.missionStatement);
    }
    builder.addStatement("$N.println()", out);
    builder.addStatement("$N.decrementIndent()", out);

    // Synopsis
    builder.addStatement("$N.println($S)", out, "SYNOPSIS");
    builder.addStatement("$N.incrementIndent()", out);
    builder.addStatement("$N.println(synopsis())", out);
    builder.addStatement("$N.println()", out);
    builder.addStatement("$N.decrementIndent()", out);

    // Description
    builder.addStatement("$N.println($S)", out, "DESCRIPTION");
    if (!context.overview.isEmpty()) {
      builder.addStatement("$N.incrementIndent()", out);
      for (String line : context.overview) {
        if (line.isEmpty()) {
          builder.addStatement("$N.println()", out);
        } else {
          builder.addStatement("$N.println($S)", out, line);
        }
      }
      builder.addStatement("$N.decrementIndent()", out);
    }

    // Positional parameters
    builder.addStatement("$N.println()", out);
    if (!context.positionalParamTypes.isEmpty()) {
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      builder.beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type);
      builder.beginControlFlow("if ($N.positional())", optionParam)
          .addStatement("printDescription($N)", optionParam)
          .addStatement("$N.println()", out)
          .endControlFlow();
      builder.endControlFlow();
    }

    // Options
    if (!context.nonpositionalParamTypes.isEmpty() || context.addHelp) {
      builder.addStatement("$N.println($S)", out, "OPTIONS");
    }

    if (!context.nonpositionalParamTypes.isEmpty()) {
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      builder.beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type);
      builder.beginControlFlow("if (!$N.positional())", optionParam)
          .addStatement("$N.incrementIndent()", out)
          .addStatement("printDescription($N)", optionParam)
          .addStatement("$N.println()", out)
          .addStatement("$N.decrementIndent()", out)
          .endControlFlow();
      builder.endControlFlow();
    }

    // Help
    if (context.addHelp) {
      builder.addStatement("$N.incrementIndent()", out)
          .addStatement("$N.println($S)", out, "--help")
          .addStatement("$N.incrementIndent()", out)
          .addStatement("$N.println($S)", out, "Print this help page.")
          .addStatement("$N.println($S)", out, "The help flag may only be passed as the first argument.")
          .addStatement("$N.println($S)", out, "Any further arguments will be ignored.")
          .addStatement("$N.println()", out)
          .addStatement("$N.decrementIndent()", out)
          .addStatement("$N.decrementIndent()", out);
    }

    return builder.build();
  }


  private MethodSpec printDescriptionMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("printDescription");
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec lineParam = ParameterSpec.builder(STRING, "line").build();
    spec.addParameter(optionParam);

    spec.addStatement("$N.println($N.describe())", out, optionParam);

    spec.addStatement("$N.incrementIndent()", out);
    spec.beginControlFlow("for ($T $N : $N.description)", STRING, lineParam, optionParam)
        .addStatement("$N.println($N)", out, lineParam)
        .endControlFlow();
    spec.addStatement("$N.decrementIndent()", out);
    return spec.build();
  }

  private MethodSpec synopsisMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("synopsis")
        .returns(STRING);

    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();

    builder.addStatement("$T $N = new $T($S)",
        StringJoiner.class, joiner, StringJoiner.class, " ");

    Map<Boolean, List<Param>> partitionedNonpos = context.parameters.stream()
        .filter(p -> !p.isPositional())
        .collect(partitioningBy(p -> p.required));

    List<Param> requiredNonpos = partitionedNonpos.get(true);
    List<Param> optionalNonpos = partitionedNonpos.get(false);

    List<Param> positional = context.parameters.stream()
        .filter(Param::isPositional)
        .collect(toList());

    builder.addStatement("$N.add($S)", joiner, context.programName);

    if (!optionalNonpos.isEmpty()) {
      builder.addStatement("$N.add($S)", joiner, "[<options>]");
    }

    for (Param param : requiredNonpos) {
      builder.addStatement("$N.add($T.$L.example())", joiner,
          context.optionType(), param.enumConstant());
    }

    for (Param param : positional) {
      PositionalOrder positionalOrder = param.positionalOrder();
      if (positionalOrder == null) {
        continue;
      }
      switch (positionalOrder) {
        case REQUIRED:
          builder.addStatement("$N.add($S + $S + $S)", joiner, "<",
              param.descriptionArgumentName(), ">");
          break;
        case OPTIONAL:
          builder.addStatement("$N.add($S + $S + $S)", joiner, "[<",
              param.descriptionArgumentName(), ">]");
          break;
        case LIST:
          builder.addStatement("$N.add($S + $S + $S)", joiner, context.allowEscape() ? "[[--] <" : "[<",
              param.descriptionArgumentName(), ">]");
          break;
        default:
          throw new AssertionError();
      }
    }

    builder.addStatement("return $N.toString()", joiner);

    return builder.build();
  }

  private CodeBlock parseMethodTryBlock(
      ParameterSpec args) {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec result = ParameterSpec.builder(optionalOfSubtype(TypeName.get(context.sourceType.asType())), "result")
        .build();
    builder.addStatement("$T $N = parseList($T.asList($N))",
        result.type, result, Arrays.class, args);

    builder.beginControlFlow("if (!$N.isPresent())", result)
        .addStatement("printUsage()")
        .addStatement("$N.flush()", out)
        .endControlFlow();

    builder.addStatement("return $N.map($T.identity())",
        result, Function.class);
    return builder.build();
  }

  private CodeBlock parseMethodCatchBlock(ParameterSpec e) {
    CodeBlock.Builder builder = CodeBlock.builder();
    if (context.addHelp) {
      builder.addStatement("$N.println($S)", out, "Usage:");
      builder.addStatement("$N.incrementIndent()", out);
      builder.addStatement("$N.println(synopsis())", out);
      builder.addStatement("$N.decrementIndent()", out);
      builder.addStatement("$N.println()", out);
      builder.addStatement("$N.println($S)", out, "Error:");
      builder.addStatement("$N.incrementIndent()", out);
      builder.addStatement("$N.println($N.getMessage())", out, e);
      builder.addStatement("$N.decrementIndent()", out);
      builder.addStatement("$N.println()", out);
      builder.addStatement("$N.println($T.format($S, $S))", out, String.class, "Try '%s --help' for more information.", context.programName);
      builder.addStatement("$N.println()", out);
    } else {
      builder.addStatement("printUsage()");
      builder.addStatement("$N.println($N.getMessage())", out, e);
    }
    builder.addStatement("$N.flush()", out);
    builder.addStatement("return $T.empty()", Optional.class);
    return builder.build();
  }

  private MethodSpec parseListMethod() {

    ParameterSpec helper = ParameterSpec.builder(context.helperType(), "helper").build();
    ParameterSpec tokens = ParameterSpec.builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec isFirst = ParameterSpec.builder(BOOLEAN, "first").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("parseList")
        .addParameter(tokens)
        .returns(optionalOfSubtype(TypeName.get(context.sourceType.asType())));

    builder.addStatement("$T $N = $L", BOOLEAN, isFirst, true);
    builder.addStatement("$T $N = new $T()", helper.type, helper, helper.type);
    builder.addStatement("$T $N = $N.iterator()", STRING_ITERATOR, it, tokens);

    if (context.hasPositional()) {
      builder.addStatement("$T $N = new $T<>()",
          LIST_OF_STRING, this.helper.positionalParameter, ArrayList.class);
    }

    builder.beginControlFlow("while ($N.hasNext())", it)
        .addCode(codeInsideParsingLoop(helper, it, isFirst))
        .endControlFlow();

    builder.addStatement(returnFromParseExpression(helper));
    return builder.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec helperParam,
      ParameterSpec it,
      ParameterSpec isFirst) {

    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.addHelp) {
      builder.beginControlFlow("if ($N && $S.equals($N))", isFirst, "--help", token)
          .addStatement("return $T.empty()", Optional.class)
          .endControlFlow();
    }

    builder.addStatement("$N = $L", isFirst, false);

    if (context.allowEscape()) {
      builder.beginControlFlow("if ($S.equals($N))", "--", token);
      if (context.hasPositional()) {
        builder.addStatement("$N.forEachRemaining($N::add)", it, helper.positionalParameter);
      }
      builder.addStatement(returnFromParseExpression(helperParam))
          .endControlFlow();
    }

    // handle empty token
    builder.beginControlFlow("if ($N.isEmpty())", token);
    if (context.hasPositional()) {
      builder.addStatement("$N.add($N)", helper.positionalParameter, token)
          .addStatement("continue");
    } else {
      builder.addStatement(throwInvalidOptionStatement(token));
    }
    builder.endControlFlow();

    builder.addStatement("$T $N = $N.$N($N)", context.optionType(), optionParam, helperParam, this.helper.readRegularOptionMethod, token);

    builder.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N($N, $N, $N)",
            helperParam, helper.readMethod, optionParam, token, it)
        .addStatement("continue")
        .endControlFlow();

    // handle unknown token
    if (context.strict) {
      // disallow tokens that start with a dash
      builder.beginControlFlow("if ($N.charAt(0) == '-')", token)
          .addStatement(throwInvalidOptionStatement(token))
          .endControlFlow();
    }
    if (context.hasPositional()) {
      builder.addStatement("$N.add($N)", helper.positionalParameter, token);
    } else {
      builder.addStatement(throwInvalidOptionStatement(token));
    }

    return builder.build();
  }

  private CodeBlock throwInvalidOptionStatement(ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .build();
  }

  private CodeBlock returnFromParseExpression(ParameterSpec helper) {
    if (context.hasPositional()) {
      return CodeBlock.builder()
          .add("return $N.build($N)", helper, this.helper.positionalParameter)
          .build();
    }
    return CodeBlock.builder().add("return $N.build()", helper).build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec outParam = ParameterSpec.builder(out.type, out.name).build();

    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", out, outParam)
        .addParameter(outParam)
        .build();
  }
}
