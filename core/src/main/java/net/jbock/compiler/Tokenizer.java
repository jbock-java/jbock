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
import static net.jbock.com.squareup.javapoet.TypeName.INT;
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
        .addMethod(describeMethod())
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
      builder.beginControlFlow("for ($T $N: $T.values())",
          optionParam.type, optionParam, optionParam.type)
          .beginControlFlow("if ($N.$N)",
              optionParam, option.positionalField)
          .addStatement("describe($N)", optionParam)
          .addStatement("$N.println()", out)
          .endControlFlow()
          .endControlFlow();
    }

    // Options
    if (!context.nonpositionalParamTypes.isEmpty() || context.addHelp) {
      builder.addStatement("$N.println($S)", out, "OPTIONS");
    }

    if (!context.nonpositionalParamTypes.isEmpty()) {
      ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
      builder.beginControlFlow("for ($T $N: $T.values())",
          optionParam.type, optionParam, optionParam.type)
          .beginControlFlow("if (!$N.$N)",
              optionParam, option.positionalField)
          .addStatement("$N.incrementIndent()", out)
          .addStatement("describe($N)", optionParam)
          .addStatement("$N.println()", out)
          .addStatement("$N.decrementIndent()", out)
          .endControlFlow()
          .endControlFlow();
    }

    // Help
    if (context.addHelp) {
      builder.addStatement("$N.incrementIndent()", out);
      builder.addStatement("$N.println($S)", out, "--help");
      builder.addStatement("$N.incrementIndent()", out);
      builder.addStatement("$N.println($S)", out, "Print this help page.");
      builder.addStatement("$N.println($S)", out, "The help flag may only be passed as the first argument.");
      builder.addStatement("$N.println($S)", out, "Any further arguments will be ignored.");
      builder.addStatement("$N.println()", out);
      builder.addStatement("$N.decrementIndent()", out);
      builder.addStatement("$N.decrementIndent()", out);
    }

    return builder.build();
  }


  private MethodSpec describeMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("describe");
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec lineParam = ParameterSpec.builder(STRING, "line").build();
    spec.addParameter(optionParam);

    spec.beginControlFlow("if ($N.$N)", optionParam, option.positionalField)
        .addStatement("$N.println($N)", out, optionParam)
        .endControlFlow();
    if (context.nonpositionalParamTypes.contains(OptionType.FLAG)) {
      spec.beginControlFlow("else if ($N.$N == $T.$L)", optionParam, option.typeField, context.optionTypeType(), OptionType.FLAG)
          .addStatement("$N.println($N.describeParam($S))", out, optionParam, "")
          .endControlFlow();
    }
    spec.beginControlFlow("else")
        .addStatement("$N.println($N.describeParam($T.format($S, $N.$N)))", out, optionParam, String.class, " <%s>", optionParam, option.argumentNameField)
        .endControlFlow();

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
        .collect(partitioningBy(p -> p.paramType.required));

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
      switch (param.positionalType().positionalOrder) {
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
    ParameterSpec stopword = ParameterSpec.builder(STRING, "stopword").build();
    ParameterSpec count = ParameterSpec.builder(INT, "count").build();

    MethodSpec.Builder builder = MethodSpec.methodBuilder("parseList")
        .addParameter(tokens)
        .returns(optionalOfSubtype(TypeName.get(context.sourceType.asType())));

    builder.addStatement("$T $N = 0", INT, count);
    builder.addStatement("$T $N = new $T()", helper.type, helper, helper.type);
    builder.addStatement("$T $N = $N.iterator()", STRING_ITERATOR, it, tokens);

    if (context.allowEscape()) {
      builder.addStatement("$T $N = $S", STRING, stopword, "--");
    }

    if (context.hasPositional()) {
      builder.addStatement("$T $N = new $T<>()",
          LIST_OF_STRING, this.helper.positionalParameter, ArrayList.class);
    }

    builder.beginControlFlow("while ($N.hasNext())", it)
        .addCode(codeInsideParsingLoop(helper, it, stopword, count))
        .endControlFlow();

    builder.addStatement(returnFromParseExpression(helper));
    return builder.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec helper,
      ParameterSpec it,
      ParameterSpec dd,
      ParameterSpec count) {

    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.addHelp) {
      builder.beginControlFlow("if ($N++ == 0 && !$N.hasNext() && $S.equals($N))", count, it, "--help", token)
          .addStatement("return $T.empty()", Optional.class)
          .endControlFlow();
    }

    if (context.allowEscape()) {
      builder.beginControlFlow("if ($N.equals($N))", dd, token);
      if (context.hasPositional()) {
        builder.addStatement("$N.forEachRemaining($N::add)", it, this.helper.positionalParameter);
      }
      builder.addStatement(returnFromParseExpression(helper))
          .endControlFlow();
    }

    builder.addStatement("$T $N = $N.$N($N)", context.optionType(), optionParam, helper, this.helper.readRegularOptionMethod, token);

    builder.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N($N, $N, $N)",
            helper, this.helper.readMethod, optionParam, token, it)
        .endControlFlow();

    builder.beginControlFlow("else");
    // handle unknown token
    if (context.strict) {
      builder.beginControlFlow("if (!$N.isEmpty() && $N.charAt(0) == '-')",
          token, token)
          .addStatement(throwInvalidOptionStatement(token))
          .endControlFlow();
    }
    if (context.hasPositional()) {
      builder.addStatement("$N.add($N)", this.helper.positionalParameter, token);
    } else {
      builder.addStatement(throwInvalidOptionStatement(token));
    }
    builder.endControlFlow();

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
