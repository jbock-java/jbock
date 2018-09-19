package net.jbock.compiler;

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

import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.com.squareup.javapoet.TypeName.BOOLEAN;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOfSubtype;

final class Tokenizer {

  private final Context context;
  private final Helper helper;

  private final FieldSpec out;
  private final FieldSpec err;

  private Tokenizer(Context context, Helper helper, FieldSpec out, FieldSpec err) {
    this.out = out;
    this.context = context;
    this.helper = helper;
    this.err = err;
  }

  static Tokenizer create(Context context, Helper helper) {
    FieldSpec out = FieldSpec.builder(context.indentPrinterType(), "out")
        .addModifiers(FINAL).build();
    FieldSpec err = FieldSpec.builder(context.indentPrinterType(), "err")
        .addModifiers(FINAL).build();
    return new Tokenizer(context, helper, out, err);
  }


  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.tokenizerType())
        .addModifiers(STATIC, PRIVATE)
        .addMethod(parseMethod())
        .addMethod(parseListMethod())
        .addMethod(privateConstructor())
        .addMethod(printUsageMethod())
        .addMethod(synopsisMethod())
        .addMethod(printDescriptionMethod())
        .addField(err);
    if (context.addHelp) {
      spec.addField(out);
    }
    return spec.build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = ParameterSpec.builder(Constants.STRING_ARRAY, "args")
        .build();
    ParameterSpec e = ParameterSpec.builder(RuntimeException.class, "e")
        .build();
    MethodSpec.Builder builder = MethodSpec.methodBuilder("parse");

    builder.beginControlFlow("try")
        .addCode(parseMethodTryBlock(args))
        .endControlFlow();

    builder.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addCode(parseMethodCatchBlock(e))
        .endControlFlow();

    return builder
        .addParameter(args)
        .returns(context.parseResultType())
        .build();
  }

  private MethodSpec printUsageMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("printUsage");
    ParameterSpec outStream = ParameterSpec.builder(context.indentPrinterType(), "outStream").build();

    // Name
    builder.addStatement("$N.println($S)", outStream, "NAME");
    builder.addStatement("$N.incrementIndent()", outStream);
    if (context.missionStatement.isEmpty()) {
      builder.addStatement("$N.println($S)", outStream, context.programName);
    } else {
      builder.addStatement("$N.println($T.format($S, $S, $S))",
          outStream, String.class, "%s - %s", context.programName, context.missionStatement);
    }
    builder.addStatement("$N.println()", outStream);
    builder.addStatement("$N.decrementIndent()", outStream);

    // Synopsis
    builder.addStatement("$N.println($S)", outStream, "SYNOPSIS");
    builder.addStatement("$N.incrementIndent()", outStream);
    builder.addStatement("$N.println(synopsis())", outStream);
    builder.addStatement("$N.println()", outStream);
    builder.addStatement("$N.decrementIndent()", outStream);

    // Description
    builder.addStatement("$N.println($S)", outStream, "DESCRIPTION");
    if (!context.overview.isEmpty()) {
      builder.addStatement("$N.incrementIndent()", outStream);
      for (String line : context.overview) {
        if (line.isEmpty()) {
          builder.addStatement("$N.println()", outStream);
        } else {
          builder.addStatement("$N.println($S)", outStream, line);
        }
      }
      builder.addStatement("$N.decrementIndent()", outStream);
    }

    // Positional parameters
    builder.addStatement("$N.println()", outStream);
    if (!context.positionalParamTypes.isEmpty()) {
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      builder.beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type);
      builder.beginControlFlow("if ($N.positional())", optionParam)
          .addStatement("printDescription($N, $N)", outStream, optionParam)
          .addStatement("$N.println()", outStream)
          .endControlFlow();
      builder.endControlFlow();
    }

    // Options
    if (!context.nonpositionalParamTypes.isEmpty() || context.addHelp) {
      builder.addStatement("$N.println($S)", outStream, "OPTIONS");
    }

    if (!context.nonpositionalParamTypes.isEmpty()) {
      builder.addStatement("$N.incrementIndent()", outStream);
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      builder.beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type);
      builder.beginControlFlow("if (!$N.positional())", optionParam)
          .addStatement("printDescription($N, $N)", outStream, optionParam)
          .addStatement("$N.println()", outStream)
          .endControlFlow();
      builder.endControlFlow();
      builder.addStatement("$N.decrementIndent()", outStream);
    }

    // Help
    if (context.addHelp) {
      builder.addStatement("$N.incrementIndent()", outStream)
          .addStatement("$N.println($S)", outStream, "--help")
          .addStatement("$N.incrementIndent()", outStream)
          .addStatement("$N.println($S)", outStream, "Print this help page.")
          .addStatement("$N.println($S)", outStream, "The help flag may only be passed as the first argument.")
          .addStatement("$N.println($S)", outStream, "Any further arguments will be ignored.")
          .addStatement("$N.println()", outStream)
          .addStatement("$N.decrementIndent()", outStream)
          .addStatement("$N.decrementIndent()", outStream);
    }

    return builder.addModifiers(STATIC).addParameter(outStream).build();
  }


  private MethodSpec printDescriptionMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("printDescription");
    ParameterSpec outStream = ParameterSpec.builder(context.indentPrinterType(), "outStream").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec lineParam = ParameterSpec.builder(STRING, "line").build();
    spec.addParameter(outStream);
    spec.addParameter(optionParam);

    spec.beginControlFlow("if ($N.positional())", optionParam)
        .addStatement("$N.println($N.describe().toUpperCase())", outStream, optionParam)
        .endControlFlow()
        .beginControlFlow("else")
        .addStatement("$N.println($N.describe())", outStream, optionParam)
        .endControlFlow();

    spec.addStatement("$N.incrementIndent()", outStream);
    spec.beginControlFlow("for ($T $N : $N.description)", STRING, lineParam, optionParam)
        .addStatement("$N.println($N)", outStream, lineParam)
        .endControlFlow();
    spec.addStatement("$N.decrementIndent()", outStream);
    return spec.addModifiers(STATIC).build();
  }

  private MethodSpec synopsisMethod() {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("synopsis")
        .returns(STRING);

    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();

    builder.addStatement("$T $N = new $T($S)",
        StringJoiner.class, joiner, StringJoiner.class, " ");

    Map<Boolean, List<Param>> partitionedOptions = context.parameters.stream()
        .filter(Param::isOption)
        .collect(partitioningBy(Param::required));

    List<Param> requiredNonpos = partitionedOptions.get(true);
    List<Param> optionalNonpos = partitionedOptions.get(false);

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
          builder.addStatement("$N.add($S)", joiner, "<" +
              param.descriptionArgumentName() + ">");
          break;
        case OPTIONAL:
          builder.addStatement("$N.add($S)", joiner, "[<" +
              param.descriptionArgumentName() + ">]");
          break;
        case LIST:
          builder.addStatement("$N.add($S)", joiner, context.allowEscape() ?
              "[[--] <" + param.descriptionArgumentNameWithDots() + ">]" :
              "[<" + param.descriptionArgumentNameWithDots() + ">]");
          break;
        default:
          throw new AssertionError();
      }
    }

    builder.addStatement("return $N.toString()", joiner);

    return builder.addModifiers(STATIC).build();
  }

  private CodeBlock parseMethodTryBlock(
      ParameterSpec args) {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec result = ParameterSpec.builder(optionalOfSubtype(TypeName.get(context.sourceType.asType())), "result")
        .build();
    builder.addStatement("$T $N = parseList($T.asList($N))",
        result.type, result, Arrays.class, args);

    FieldSpec outStream = context.addHelp ? out : err;
    builder.beginControlFlow("if ($N.isPresent())", result)
        .addStatement("return new $T($N.get(), true)", context.parseResultType(), result)
        .endControlFlow();

    builder.addStatement("printUsage($N)", outStream)
        .addStatement("$N.flush()", outStream)
        .addStatement("return new $T(null, true)", context.parseResultType());
    return builder.build();
  }

  private CodeBlock parseMethodCatchBlock(ParameterSpec e) {
    CodeBlock.Builder builder = CodeBlock.builder();
    if (context.addHelp) {
      builder.addStatement("$N.println($S)", err, "Usage:");
      builder.addStatement("$N.incrementIndent()", err);
      builder.addStatement("$N.println(synopsis())", err);
      builder.addStatement("$N.decrementIndent()", err);
      builder.addStatement("$N.println()", err);

      builder.addStatement("$N.println($S)", err, "Error:");
      builder.addStatement("$N.incrementIndent()", err);
      builder.addStatement("$N.println($N.getMessage())", err, e);
      builder.addStatement("$N.decrementIndent()", err);
      builder.addStatement("$N.println()", err);
      builder.addStatement("$N.println($T.format($S, $S))", err, String.class, "Try '%s --help' for more information.", context.programName);
      builder.addStatement("$N.println()", err);
    } else {
      builder.addStatement("printUsage($N)", err);
      builder.addStatement("$N.println($N.getMessage())", err, e);
    }
    builder.addStatement("$N.flush()", err);
    builder.addStatement("return new $T(null, false)", context.parseResultType());
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
    ParameterSpec errParam = ParameterSpec.builder(err.type, err.name).build();

    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    spec.addStatement("this.$N = $N", err, errParam)
        .addParameter(outParam)
        .addParameter(errParam);
    if (context.addHelp) {
      spec.addStatement("this.$N = $N", out, outParam);
    }
    return spec.build();
  }
}
