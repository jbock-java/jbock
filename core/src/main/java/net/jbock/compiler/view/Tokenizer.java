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
    return spec.build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec args = builder(Constants.STRING_ARRAY, "args").build();
    ParameterSpec e = builder(RuntimeException.class, "e").build();
    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse");

    context.helpRequestedType().ifPresent(helpRequestedType ->
        spec.beginControlFlow("if ($N.length >= 1 && $S.equals($N[0]))", args, "--help", args)
            .addStatement("return new $T(synopsis(), buildRows())", helpRequestedType)
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

  private MethodSpec buildRowsMethod() {
    ParameterSpec rows = builder(Constants.listOf(ENTRY_STRING_STRING), "rows").build();
    ParameterSpec optionParam = builder(context.optionType(), "option").build();
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
    ParameterSpec option = builder(context.optionType(), "option").build();
    ParameterSpec message = builder(STRING, "message").build();
    ParameterSpec description = builder(STRING, "description").build();
    return MethodSpec.methodBuilder("printDescription")
        .addParameter(option)
        .addStatement("$T $N = $N.getMessage($N.bundleKey, $N.description)",
            STRING, message, messages, option, option)
        .addStatement("$T $N", STRING, description)
        .beginControlFlow("if ($N.positionalIndex.isPresent())", option)
        .addStatement("$N = $N.name().toLowerCase($T.US)", description, option, Locale.class)
        .endControlFlow()
        .beginControlFlow("else if ($N.flag)", option)
        .addStatement("$N = $N.describeParam($S)", description, option, "")
        .endControlFlow()
        .beginControlFlow("else")
        .addStatement("$N = $N.describeParam(' ' + $N.name())", description, option, option)
        .endControlFlow()
        .addStatement("return new $T($N, $N)",
            ParameterizedTypeName.get(ClassName.get(SimpleImmutableEntry.class), STRING, STRING),
            description, message)
        .returns(ENTRY_STRING_STRING)
        .build();
  }

  private MethodSpec synopsisMethod() {
    MethodSpec.Builder spec = MethodSpec.methodBuilder("synopsis")
        .returns(STRING);

    ParameterSpec joiner = builder(StringJoiner.class, "joiner").build();

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

    ParameterSpec stateParam = builder(context.parserStateType(), "state").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();

    MethodSpec.Builder spec = MethodSpec.methodBuilder("parse")
        .addParameter(it)
        .returns(context.sourceElement());

    ParameterSpec positionParam = builder(INT, "position").build();

    spec.addStatement("$T $N = $L", positionParam.type, positionParam, 0);
    spec.addStatement("$T $N = new $T()", stateParam.type, stateParam, stateParam.type);

    spec.beginControlFlow("while ($N.hasNext())", it)
        .addCode(codeInsideParsingLoop(stateParam, positionParam, it))
        .endControlFlow();

    spec.addStatement("return $N.build()", stateParam);
    return spec.build();
  }

  private CodeBlock codeInsideParsingLoop(
      ParameterSpec stateParam,
      ParameterSpec positionParam,
      ParameterSpec it) {

    ParameterSpec optionParam = builder(context.optionType(), "option").build();
    ParameterSpec token = builder(STRING, "token").build();

    CodeBlock.Builder spec = CodeBlock.builder();
    spec.addStatement("$T $N = $N.next()", STRING, token, it);

    if (context.allowEscape()) {
      ParameterSpec t = builder(STRING, "t").build();
      spec.beginControlFlow("if ($S.equals($N))", "--", token);

      spec.beginControlFlow("while ($N.hasNext())", it)
          .addStatement("$T $N = $N.next()", STRING, t, it)
          .beginControlFlow("if ($N >= $N.$N.size())", positionParam, stateParam, state.positionalParsersField())
          .addStatement(throwInvalidOptionStatement(t))
          .endControlFlow()
          .addStatement("$N += $N.$N.get($N).read($N)", positionParam, stateParam, state.positionalParsersField(), positionParam, t)
          .endControlFlow()
          .addStatement("return $N.build()", stateParam);

      spec.endControlFlow();
    }

    spec.addStatement("$T $N = $N.$N($N)", context.optionType(), optionParam, stateParam, state.tryReadOption(), token);

    spec.beginControlFlow("if ($N != null)", optionParam)
        .addStatement("$N.$N.get($N).read($N, $N)", stateParam, state.parsersField(), optionParam, token, it)
        .addStatement("continue")
        .endControlFlow();

    // handle unknown token
    spec.beginControlFlow("if (!$N.isEmpty() && $N.charAt(0) == '-')", token, token)
        .addStatement(throwInvalidOptionStatement(token))
        .endControlFlow();

    spec.beginControlFlow("if ($N >= $N.$N.size())", positionParam, stateParam, state.positionalParsersField())
        .addStatement(throwInvalidOptionStatement(token))
        .endControlFlow()
        .addStatement("$N += $N.$N.get($N).read($N)", positionParam, stateParam, state.positionalParsersField(), positionParam, token);

    return spec.build();
  }

  static CodeBlock throwInvalidOptionStatement(ParameterSpec token) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", IllegalArgumentException.class,
            "Invalid option: ", token)
        .build();
  }

  private MethodSpec privateConstructor() {
    ParameterSpec messagesParam = builder(messages.type, messages.name).build();
    return constructorBuilder().addParameter(messagesParam)
        .addStatement("this.$N = $N", messages, messagesParam)
        .build();
  }
}
