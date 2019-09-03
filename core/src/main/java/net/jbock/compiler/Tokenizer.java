package net.jbock.compiler;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.LIST_OF_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Util.optionalOfSubtype;

final class Tokenizer {

  // special bundle keys that may not be used on a parameter
  private static final String SPECIAL_KEY_DESCRIPTION = "jbock.description";
  private static final String SPECIAL_KEY_MISSION = "jbock.mission";
  private static final String SPECIAL_KEY_HELP = "jbock.help";

  private final Context context;

  private final Helper helper;

  private final FieldSpec out;

  private final FieldSpec err;

  private final FieldSpec messages;

  private Tokenizer(Context context, Helper helper, FieldSpec out, FieldSpec err, FieldSpec messages) {
    this.out = out;
    this.context = context;
    this.helper = helper;
    this.err = err;
    this.messages = messages;
  }

  static Tokenizer create(Context context, Helper helper) {
    FieldSpec out = FieldSpec.builder(context.indentPrinterType(), "out")
        .addModifiers(FINAL).build();
    FieldSpec err = FieldSpec.builder(context.indentPrinterType(), "err")
        .addModifiers(FINAL).build();
    FieldSpec messages = FieldSpec.builder(context.messagesType(), "messages")
        .addModifiers(FINAL).build();
    return new Tokenizer(context, helper, out, err, messages);
  }


  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.tokenizerType())
        .addModifiers(STATIC, PRIVATE)
        .addMethod(parseMethod())
        .addMethod(parseMethodOverload())
        .addMethod(privateConstructor())
        .addMethod(printUsageMethod())
        .addMethod(synopsisMethod())
        .addMethod(printDescriptionMethod())
        .addField(err)
        .addField(out)
        .addField(messages);
    return spec.build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = ParameterSpec.builder(Constants.STRING_ARRAY, "args")
        .build();
    ParameterSpec e = ParameterSpec.builder(RuntimeException.class, "e")
        .build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");

    spec.beginControlFlow("try")
        .addCode(parseMethodTryBlock(args))
        .endControlFlow();

    spec.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addCode(parseMethodCatchBlock(e))
        .endControlFlow();

    spec.beginControlFlow("finally")
        .addStatement("$N.flush()", err)
        .addStatement("$N.flush()", out)
        .endControlFlow();


    return spec
        .addParameter(args)
        .returns(context.parseResultType())
        .build();
  }

  private MethodSpec printUsageMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("printUsage");

    // Program Name
    spec.addStatement("$N.println($S)", out, "NAME");
    spec.addStatement("$N.incrementIndent()", out);

    // Mission Statement
    ParameterSpec missionStatement = ParameterSpec.builder(STRING, "missionStatement").build();
    spec.addStatement("$T $N = $N.getMessage($S, $S)",
        missionStatement.type, missionStatement, messages, SPECIAL_KEY_MISSION, context.missionStatement);
    spec.beginControlFlow("if ($N.isEmpty())", missionStatement)
        .addStatement("$N.println($S)", out, context.programName)
        .endControlFlow();
    spec.beginControlFlow("else")
        .addStatement("$N.println($T.format($S, $S, $N))",
            out, String.class, "%s - %s", context.programName, missionStatement)
        .endControlFlow();
    spec.addStatement("$N.println()", out);
    spec.addStatement("$N.decrementIndent()", out);

    // Synopsis
    spec.addStatement("$N.println($S)", out, "SYNOPSIS");
    spec.addStatement("$N.incrementIndent()", out);
    spec.addStatement("$N.println(synopsis())", out);
    spec.addStatement("$N.println()", out);
    spec.addStatement("$N.decrementIndent()", out);

    // Description
    spec.addStatement("$N.println($S)", out, "DESCRIPTION");
    spec.addStatement("$N.incrementIndent()", out);
    ParameterSpec overview = ParameterSpec.builder(LIST_OF_STRING, "descriptionFromJavadoc").build();
    spec.addStatement("$T $N = new $T<>()", overview.type, overview, ArrayList.class);
    for (String line : context.description) {
      spec.addStatement("$N.add($S)", overview, line);
    }
    ParameterSpec line = ParameterSpec.builder(STRING, "line").build();
    spec.beginControlFlow("for ($T $N : $N.getMessage($S, $N))",
        line.type, line, messages, SPECIAL_KEY_DESCRIPTION, overview)
        .addStatement("$N.println($N)", out, line)
        .endControlFlow();
    spec.addStatement("$N.decrementIndent()", out);

    // Positional parameters
    spec.addStatement("$N.println()", out);
    if (!context.positionalParamTypes.isEmpty()) {
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      spec.beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type);
      spec.beginControlFlow("if ($N.positional())", optionParam)
          .addStatement("printDescription($N)", optionParam)
          .addStatement("$N.println()", out)
          .endControlFlow();
      spec.endControlFlow();
    }

    // Options
    if (!context.nonpositionalParamTypes.isEmpty() || context.addHelp) {
      spec.addStatement("$N.println($S)", out, "OPTIONS");
    }

    if (!context.nonpositionalParamTypes.isEmpty()) {
      spec.addStatement("$N.incrementIndent()", out);
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      spec.beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type);
      spec.beginControlFlow("if (!$N.positional())", optionParam)
          .addStatement("printDescription($N)", optionParam)
          .addStatement("$N.println()", out)
          .endControlFlow();
      spec.endControlFlow();
      spec.addStatement("$N.decrementIndent()", out);
    }

    // Help
    if (context.addHelp) {
      describeHelpParameter(spec);
    }

    return spec.build();
  }

  private void describeHelpParameter(MethodSpec.Builder spec) {
    ParameterSpec defaultHelpText = ParameterSpec.builder(LIST_OF_STRING, "defaultHelp").build();
    spec.addStatement("$T $N = new $T<>()", defaultHelpText.type, defaultHelpText, ArrayList.class);
    spec.addStatement("$N.add($S)", defaultHelpText, "Print this help page.");
    spec.addStatement("$N.add($S)", defaultHelpText, "The help flag may only be passed as the first argument.");
    spec.addStatement("$N.add($S)", defaultHelpText, "Any further arguments will be ignored.");
    spec.addStatement("$N.incrementIndent()", out);
    spec.addStatement("$N.println($S)", out, "--help");
    spec.addStatement("$N.incrementIndent()", out);
    ParameterSpec line = ParameterSpec.builder(STRING, "line").build();
    spec.beginControlFlow("for ($T $N : $N.getMessage($S, $N))",
        line.type, line, messages, SPECIAL_KEY_HELP, defaultHelpText)
        .addStatement("$N.println($N)", out, line)
        .endControlFlow();
    spec.addStatement("$N.println()", out);
    spec.addStatement("$N.decrementIndent()", out);
    spec.addStatement("$N.decrementIndent()", out);
  }


  private MethodSpec printDescriptionMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("printDescription");
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec lineParam = ParameterSpec.builder(STRING, "line").build();
    spec.addParameter(optionParam);

    spec.beginControlFlow("if ($N.positional())", optionParam)
        .addStatement("$N.println($N.describe().toUpperCase())", out, optionParam)
        .endControlFlow()
        .beginControlFlow("else")
        .addStatement("$N.println($N.describe())", out, optionParam)
        .endControlFlow();

    spec.addStatement("$N.incrementIndent()", out);
    spec.beginControlFlow("for ($T $N : $N.getMessage($N.bundleKey.toLowerCase(), $N.description))",
        STRING, lineParam, messages, optionParam, optionParam)
        .addStatement("$N.println($N)", out, lineParam)
        .endControlFlow();
    spec.addStatement("$N.decrementIndent()", out);
    return spec.build();
  }

  private MethodSpec synopsisMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("synopsis")
        .returns(STRING);

    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();

    spec.addStatement("$T $N = new $T($S)",
        StringJoiner.class, joiner, StringJoiner.class, " ");

    Map<Boolean, List<Param>> partitionedOptions = context.parameters.stream()
        .filter(Param::isOption)
        .collect(partitioningBy(Param::required));

    List<Param> requiredNonpos = partitionedOptions.get(true);
    List<Param> optionalNonpos = partitionedOptions.get(false);

    List<Param> positional = context.parameters.stream()
        .filter(Param::isPositional)
        .collect(toList());

    spec.addStatement("$N.add($S)", joiner, context.programName);

    if (!optionalNonpos.isEmpty()) {
      spec.addStatement("$N.add($S)", joiner, "[<options>]");
    }

    for (Param param : requiredNonpos) {
      spec.addStatement("$N.add($T.$L.example())", joiner,
          context.optionType(), param.enumConstant());
    }

    for (Param param : positional) {
      PositionalRank positionalRank = param.positionalOrder();
      if (positionalRank == null) {
        continue;
      }
      switch (positionalRank) {
        case OPTIONAL:
          spec.addStatement("$N.add($S)", joiner, "[<" +
              param.descriptionArgumentName() + ">]");
          break;
        case REQUIRED:
          spec.addStatement("$N.add($S)", joiner, "<" +
              param.descriptionArgumentName() + ">");
          break;
        case LIST:
          spec.addStatement("$N.add($S)", joiner, context.allowEscape() ?
              "[[--] <" + param.descriptionArgumentNameWithDots() + ">]" :
              "[<" + param.descriptionArgumentNameWithDots() + ">]");
          break;
        default:
          throw new AssertionError();
      }
    }

    spec.addStatement("return $N.toString()", joiner);

    return spec.addModifiers(STATIC).build();
  }

  private CodeBlock parseMethodTryBlock(
      ParameterSpec args) {
    CodeBlock.Builder spec = CodeBlock.builder();
    ParameterSpec result = ParameterSpec.builder(optionalOfSubtype(TypeName.get(context.sourceType.asType())), "result")
        .build();
    spec.addStatement("$T $N = parse($T.asList($N).iterator())",
        result.type, result, Arrays.class, args);

    spec.beginControlFlow("if ($N.isPresent())", result)
        .addStatement("return new $T($N.get())", context.successParseResultType(), result)
        .endControlFlow();

    spec.addStatement("printUsage()")
        .addStatement("return new $T()", context.helpPrintedParseResultType());
    return spec.build();
  }

  private CodeBlock parseMethodCatchBlock(ParameterSpec e) {
    CodeBlock.Builder spec = CodeBlock.builder();
    spec.addStatement("$N.printStackTrace($N.out)", e, err);
    if (context.addHelp) {
      spec.addStatement("$N.println($S)", err, "Usage:");
      spec.addStatement("$N.incrementIndent()", err);
      spec.addStatement("$N.println(synopsis())", err);
      spec.addStatement("$N.decrementIndent()", err);
      spec.addStatement("$N.println()", err);

      spec.addStatement("$N.println($S)", err, "Error:");
      spec.addStatement("$N.incrementIndent()", err);
      spec.addStatement("$N.println($N.getMessage())", err, e);
      spec.addStatement("$N.decrementIndent()", err);
      spec.addStatement("$N.println()", err);
      spec.addStatement("$N.println($T.format($S, $S))", err, String.class, "Try '%s --help' for more information.", context.programName);
      spec.addStatement("$N.println()", err);
    } else {
      spec.addStatement("printUsage()");
      spec.addStatement("$N.println($N.getMessage())", err, e);
    }
    spec.addStatement("return new $T($N.getMessage())", context.errorParseResultType(), e);
    return spec.build();
  }

  private MethodSpec parseMethodOverload() {

    ParameterSpec helper = ParameterSpec.builder(context.helperType(), "helper").build();
    ParameterSpec tokens = ParameterSpec.builder(STRING_ITERATOR, "tokens").build();
    ParameterSpec isFirst = ParameterSpec.builder(BOOLEAN, "first").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse")
        .addParameter(tokens)
        .returns(optionalOfSubtype(TypeName.get(context.sourceType.asType())));

    spec.addStatement("$T $N = $L", BOOLEAN, isFirst, true);
    spec.addStatement("$T $N = new $T()", helper.type, helper, helper.type);

    if (context.hasPositional()) {
      spec.addStatement("$T $N = new $T<>()",
          LIST_OF_STRING, this.helper.positionalParameter, ArrayList.class);
    }

    spec.beginControlFlow("while ($N.hasNext())", tokens)
        .addCode(codeInsideParsingLoop(helper, tokens, isFirst))
        .endControlFlow();

    spec.addStatement(returnFromParseExpression(helper));
    return spec.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec helperParam,
      ParameterSpec tokens,
      ParameterSpec isFirst) {

    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder spec = CodeBlock.builder();
    spec.addStatement("$T $N = $N.next()", STRING, token, tokens);

    if (context.addHelp) {
      spec.beginControlFlow("if ($N && $S.equals($N))", isFirst, "--help", token)
          .addStatement("return $T.empty()", Optional.class)
          .endControlFlow();
    }

    spec.addStatement("$N = $L", isFirst, false);

    if (context.allowEscape()) {
      spec.beginControlFlow("if ($S.equals($N))", "--", token);
      if (context.hasPositional()) {
        spec.addStatement("$N.forEachRemaining($N::add)", tokens, helper.positionalParameter);
      }
      spec.addStatement(returnFromParseExpression(helperParam))
          .endControlFlow();
    }

    // handle empty token
    spec.beginControlFlow("if ($N.isEmpty())", token);
    if (context.hasPositional()) {
      spec.addStatement("$N.add($N)", helper.positionalParameter, token)
          .addStatement("continue");
    } else {
      spec.addStatement(throwInvalidOptionStatement(token));
    }
    spec.endControlFlow();

    spec.addStatement("$T $N = $N.$N($N)", context.optionType(), optionParam, helperParam, this.helper.readRegularOptionMethod, token);

    spec.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N($N, $N, $N)",
            helperParam, helper.readMethod, optionParam, token, tokens)
        .addStatement("continue")
        .endControlFlow();

    // handle unknown token
    if (context.strict) {
      // disallow tokens that start with a dash
      spec.beginControlFlow("if ($N.charAt(0) == '-')", token)
          .addStatement(throwInvalidOptionStatement(token))
          .endControlFlow();
    }
    if (context.hasPositional()) {
      spec.addStatement("$N.add($N)", helper.positionalParameter, token);
    } else {
      spec.addStatement(throwInvalidOptionStatement(token));
    }

    return spec.build();
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
    ParameterSpec messagesParam = ParameterSpec.builder(messages.type, messages.name).build();

    MethodSpec.Builder spec = MethodSpec.constructorBuilder();
    spec.addStatement("this.$N = $N", err, errParam)
        .addStatement("this.$N = $N", out, outParam)
        .addStatement("this.$N = $N", messages, messagesParam)
        .addParameter(outParam)
        .addParameter(errParam)
        .addParameter(messagesParam);
    return spec.build();
  }
}
