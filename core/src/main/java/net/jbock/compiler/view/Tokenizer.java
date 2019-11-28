package net.jbock.compiler.view;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;
import net.jbock.compiler.Param;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Constants.ENTRY_STRING_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ITERATOR;

/**
 * Defines the inner class Tokenizer.
 */
final class Tokenizer {

  private final Context context;

  private final ParserState state;

  private final FieldSpec messages;

  private Tokenizer(Context context, ParserState state, FieldSpec messages) {
    this.context = context;
    this.state = state;
    this.messages = messages;
  }

  static Tokenizer create(Context context, ParserState state) {
    FieldSpec messages = FieldSpec.builder(context.messagesType(), "messages")
        .addModifiers(FINAL).build();
    return new Tokenizer(context, state, messages);
  }


  TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.tokenizerType())
        .addModifiers(STATIC, PRIVATE)
        .addMethod(parseMethod())
        .addMethod(parseMethodOverloadIterator())
        .addMethod(privateConstructor())
        .addMethod(synopsisMethod())
        .addMethod(buildRowsMethod())
        .addMethod(printDescriptionMethod())
        .addField(messages);
    context.helpRequestedType()
        .map(this::printUsageMethod)
        .ifPresent(spec::addMethod);
    return spec.build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = ParameterSpec.builder(Constants.STRING_ARRAY, "args").build();
    ParameterSpec e = ParameterSpec.builder(RuntimeException.class, "e").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");

    context.helpRequestedType().ifPresent(helpPrintedType ->
        spec.beginControlFlow("if ($N.length >= 1 && $S.equals($N[0]))", args, "--help", args)
            .addStatement("return printUsage()")
            .endControlFlow());

    spec.beginControlFlow("try")
        .addStatement("return new $T(parse($T.asList($N).iterator()))", context.parsingSuccessType(), Arrays.class, args)
        .endControlFlow();

    spec.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addStatement("return new $T(synopsis(), buildRows(), $N)",
            context.parsingFailedType(), e)
        .endControlFlow();

    return spec.addParameter(args)
        .returns(context.parseResultType())
        .build();
  }

  private MethodSpec printUsageMethod(ClassName helpRequestedType) {
    return MethodSpec.methodBuilder("printUsage")
        .returns(context.parseResultType())
        .addStatement("return new $T(synopsis(), buildRows())",
            helpRequestedType).build();
  }

  private MethodSpec buildRowsMethod() {
    ParameterSpec rows = builder(Constants.listOf(ENTRY_STRING_STRING), "rows").build();
    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    return MethodSpec.methodBuilder("buildRows")
        .returns(rows.type)
        .addStatement("$T $N = new $T<>()", rows.type, rows, ArrayList.class)
        .beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type)
        .beginControlFlow("if ($N.positionalIndex.isPresent())", optionParam)
        .addStatement("$N.add(printDescription($N))", rows, optionParam)
        .endControlFlow()
        .endControlFlow()
        .beginControlFlow("for ($T $N: $T.values())", optionParam.type, optionParam, optionParam.type)
        .beginControlFlow("if (!$N.positionalIndex.isPresent())", optionParam)
        .addStatement("$N.add(printDescription($N))", rows, optionParam)
        .endControlFlow()
        .endControlFlow()
        .addStatement("return $N", rows).build();
  }

  private MethodSpec printDescriptionMethod() {
    ParameterSpec option = ParameterSpec.builder(context.optionType(), "option").build();
    return MethodSpec.methodBuilder("printDescription")
        .addParameter(option)
        .addStatement("return new $T($N.describe(), $N.getMessage($N.bundleKey, $N.description))",
            ParameterizedTypeName.get(ClassName.get(SimpleImmutableEntry.class), STRING, STRING),
            option, messages, option, option)
        .returns(ENTRY_STRING_STRING)
        .build();
  }

  private MethodSpec synopsisMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("synopsis")
        .returns(STRING);

    ParameterSpec joiner = ParameterSpec.builder(StringJoiner.class, "joiner").build();

    spec.addStatement("$T $N = new $T($S)",
        StringJoiner.class, joiner, StringJoiner.class, " ");

    Map<Boolean, List<Param>> partitionedOptions = context.parameters().stream()
        .filter(Param::isNotPositional)
        .collect(partitioningBy(Param::isRequired));

    List<Param> requiredNonpos = partitionedOptions.get(true);
    List<Param> optionalNonpos = partitionedOptions.get(false);

    List<Param> positional = context.parameters().stream()
        .filter(Param::isPositional)
        .collect(toList());

    spec.addStatement("$N.add($S)", joiner, context.programName());

    if (!optionalNonpos.isEmpty()) {
      spec.addStatement("$N.add($S)", joiner, "[options...]");
    }

    for (Param param : requiredNonpos) {
      spec.addStatement("$N.add($T.format($S, $T.$L.names.get(0), $T.$L.name().toLowerCase($T.US)))", joiner,
          String.class, "%s <%s>",
          context.optionType(), param.enumConstant(),
          context.optionType(), param.enumConstant(), Locale.class);
    }

    for (Param param : positional) {
      if (param.isOptional()) {
        spec.addStatement("$N.add($S)", joiner, "[<" + param.enumConstantLower() + ">]");
      } else if (param.isRequired()) {
        spec.addStatement("$N.add($S)", joiner, "<" + param.enumConstantLower() + ">");
      } else if (param.isRepeatable()) {
        spec.addStatement("$N.add($S)", joiner, "<" + param.enumConstantLower() + ">...");
      } else {
        throw new AssertionError("all cases handled (repeatable can't be flag)");
      }
    }

    spec.addStatement("return $N.toString()", joiner);

    return spec.addModifiers(STATIC).build();
  }

  private MethodSpec parseMethodOverloadIterator() {

    ParameterSpec stateParam = ParameterSpec.builder(context.parserStateType(), "state").build();
    ParameterSpec tokens = ParameterSpec.builder(STRING_ITERATOR, "tokens").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse")
        .addParameter(tokens)
        .returns(context.sourceElement());

    ParameterSpec positionParam = ParameterSpec.builder(INT, "position").build();

    spec.addStatement("$T $N = $L", positionParam.type, positionParam, 0);
    spec.addStatement("$T $N = new $T()", stateParam.type, stateParam, stateParam.type);

    spec.beginControlFlow("while ($N.hasNext())", tokens)
        .addCode(codeInsideParsingLoop(stateParam, positionParam, tokens))
        .endControlFlow();

    spec.addStatement("return $N.build()", stateParam);
    return spec.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec stateParam,
      ParameterSpec positionParam,
      ParameterSpec tokens) {

    ParameterSpec optionParam = ParameterSpec.builder(context.optionType(), "option").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();

    CodeBlock.Builder spec = CodeBlock.builder();
    spec.addStatement("$T $N = $N.next()", STRING, token, tokens);

    if (context.allowEscape()) {
      ParameterSpec t = ParameterSpec.builder(STRING, "t").build();
      spec.beginControlFlow("if ($S.equals($N))", "--", token);

      spec.beginControlFlow("while ($N.hasNext())", tokens)
          .addStatement("$T $N = $N.next()", STRING, t, tokens)
          .addStatement("$N += $N.$N($N, $N)", positionParam, stateParam, state.readPositionalMethod(), positionParam, t)
          .endControlFlow()
          .addStatement("return $N.build()", stateParam);

      spec.endControlFlow();
    }

    spec.addStatement("$T $N = $N.$N($N)", context.optionType(), optionParam, stateParam, state.readRegularOptionMethod(), token);

    spec.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N($N, $N, $N)", stateParam, state.readMethod(), optionParam, token, tokens)
        .addStatement("continue")
        .endControlFlow();

    // handle unknown token
    spec.beginControlFlow("if (!$N.isEmpty() && $N.charAt(0) == '-')", token, token)
        .addStatement(throwInvalidOptionStatement(token))
        .endControlFlow();

    spec.addStatement("$N += $N.$N($N, $N)", positionParam, stateParam, state.readPositionalMethod(), positionParam, token);

    return spec.build();
  }

  static CodeBlock throwInvalidOptionStatement(ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec messagesParam = ParameterSpec.builder(messages.type, messages.name).build();
    return constructorBuilder().addParameter(messagesParam)
        .addStatement("this.$N = $N", messages, messagesParam)
        .build();
  }
}
