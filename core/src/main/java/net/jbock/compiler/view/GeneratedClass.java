package net.jbock.compiler.view;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Constants;
import net.jbock.compiler.Context;
import net.jbock.compiler.Parameter;

import javax.lang.model.element.Modifier;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.squareup.javapoet.MethodSpec.constructorBuilder;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeName.INT;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.coerce.Util.addBreaks;
import static net.jbock.compiler.Constants.ENTRY_STRING_STRING;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.Constants.STRING_ITERATOR;
import static net.jbock.compiler.Constants.STRING_TO_STRING_MAP;

/**
 * Generates the *_Parser class.
 */
public final class GeneratedClass {

  private static final int DEFAULT_WRAP_AFTER = 80;
  private static final int EXITCODE_ON_ERROR = 1;

  private static final String PROJECT_URL = "https://github.com/h908714124/jbock";

  private final Context context;
  private final OptionEnum optionEnum;
  private final ParserState parserState;
  private final ParseResult parseResult;

  private final FieldSpec out = FieldSpec.builder(PrintStream.class, "out", PRIVATE)
      .initializer("$T.out", System.class).build();

  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE)
      .initializer("$T.err", System.class).build();

  private final FieldSpec maxLineWidth = FieldSpec.builder(INT, "maxLineWidth", PRIVATE)
      .initializer("$L", DEFAULT_WRAP_AFTER).build();

  private final FieldSpec messages = FieldSpec.builder(STRING_TO_STRING_MAP, "messages", PRIVATE)
      .initializer("$T.emptyMap()", Collections.class).build();

  private final FieldSpec runBeforeExit;

  private GeneratedClass(
      Context context,
      OptionEnum optionEnum,
      ParserState parserState,
      ParseResult parseResult,
      FieldSpec runBeforeExit) {
    this.context = context;
    this.optionEnum = optionEnum;
    this.parserState = parserState;
    this.parseResult = parseResult;
    this.runBeforeExit = runBeforeExit;
  }

  public static GeneratedClass create(Context context) {
    OptionEnum optionEnum = OptionEnum.create(context);
    ParserState state = ParserState.create(context, optionEnum);
    ParseResult parseResult = ParseResult.create(context);
    FieldSpec runBeforeExit = FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Consumer.class), context.parseResultType()), "runBeforeExit").addModifiers(PRIVATE)
        .initializer("r -> {}")
        .build();
    return new GeneratedClass(context, optionEnum, state, parseResult, runBeforeExit);
  }

  public TypeSpec define() {
    TypeSpec.Builder spec = TypeSpec.classBuilder(context.generatedClass())
        .addMethod(createMethod(context.getAccessModifiers()))
        .addMethod(parseMethod(context.getAccessModifiers()))
        .addMethod(maxLineWidthMethod(context.getAccessModifiers()))
        .addMethod(withMessagesMethod(context.getAccessModifiers()))
        .addMethod(withResourceBundleMethod(context.getAccessModifiers()))
        .addMethod(runBeforeExitMethod(context.getAccessModifiers()))
        .addMethod(withErrorStreamMethod(context.getAccessModifiers()));
    if (context.isHelpParameterEnabled()) {
      spec.addMethod(withHelpStreamMethod(context.getAccessModifiers()));
    }
    spec.addMethod(parseOrExitMethod(context.getAccessModifiers()))
        .addMethod(buildRowsMethod(context.getAccessModifiers()))
        .addMethod(printOnlineHelpMethod(context.getAccessModifiers()))
        .addMethod(printWrapMethod(context.getAccessModifiers()))
        .addMethod(synopsisMethod(context.getAccessModifiers()));

    // PRIVATE Methods
    spec.addMethod(parseMethodOverloadIterator())
        .addMethod(readOptionArgumentMethod());

    if (context.isHelpParameterEnabled()) {
      spec.addField(out);
    }
    spec.addFields(Arrays.asList(err, maxLineWidth, runBeforeExit, messages));

    spec.addType(parserState.define())
        .addType(Impl.define(context))
        .addType(optionEnum.define())
        .addTypes(OptionParser.define(context))
        .addTypes(ParamParser.define(context))
        .addTypes(parseResult.defineResultTypes());

    return spec.addModifiers(FINAL)
        .addModifiers(context.getAccessModifiers())
        .addMethod(constructorBuilder().addModifiers(PRIVATE).build())
        .addJavadoc(javadoc()).build();
  }

  private MethodSpec buildRowsMethod(Modifier[] accessModifiers) {
    ParameterSpec rows = builder(Constants.listOf(ENTRY_STRING_STRING), "rows").build();
    ParameterSpec optionParam = builder(context.optionType(), "option").build();
    ParameterSpec message = builder(STRING, "message").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.add("return $T.stream($T.values()).map($N -> {\n", Arrays.class, context.optionType(), optionParam).indent()
        .addStatement("$T $N = $N.getOrDefault($N.bundleKey, $T.join($S, $N.description)).trim()",
            STRING, message, messages, optionParam, String.class, " ", optionParam)
        .addStatement("return new $T($N.shape, $N)", ParameterizedTypeName.get(ClassName.get(SimpleImmutableEntry.class), STRING, STRING),
            optionParam, message)
        .unindent()
        .add("}).collect($T.toList());\n", Collectors.class);
    return methodBuilder("buildRows").returns(rows.type)
        .addCode(builder.build())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec printOnlineHelpMethod(Modifier[] accessModifiers) {
    List<Parameter> params = context.parameters();
    // 2 space padding on both sides
    int totalPadding = 4;
    int width = params.stream().map(Parameter::shape).mapToInt(String::length).max().orElse(0) + totalPadding;
    String format = "  %1$-" + (width - 2) + "s";
    ParameterSpec row = builder(ENTRY_STRING_STRING, "row").build();
    ParameterSpec printStream = builder(PrintStream.class, "printStream").build();
    ParameterSpec key = builder(STRING, "key").build();
    MethodSpec.Builder spec = methodBuilder("printOnlineHelp");
    spec.addStatement("printWrap($N, 8, $S, $S + synopsis())", printStream, "", "Usage: ");
    spec.addStatement("$N.println()", printStream);
    spec.beginControlFlow("for ($T $N : buildRows())", row.type, row)
        .addStatement("$T $N = $T.format($S, $N.getKey())", STRING, key, STRING, format, row)
        .addStatement("printWrap($N, $L, $N, $N.getValue())", printStream, width, key, row)
        .endControlFlow();
    return spec.addParameter(printStream)
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec printWrapMethod(Modifier[] accessModifiers) {
    ParameterSpec printStream = builder(PrintStream.class, "printStream").build();
    ParameterSpec continuationIndent = builder(INT, "continuationIndent").build();
    ParameterSpec i = builder(INT, "i").build();
    ParameterSpec trim = builder(STRING, "trim").build();
    ParameterSpec init = builder(STRING, "init").build();
    ParameterSpec input = builder(STRING, "input").build();
    ParameterSpec sb = builder(StringBuilder.class, "sb").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec tokens = builder(ArrayTypeName.of(String.class), "tokens").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.beginControlFlow("if ($N.isEmpty())", input)
        .addStatement("$T $N = $N.trim()", STRING, trim, init)
        .addStatement("$N.println($N.substring(0, $N.indexOf($N)) + $N)",
            printStream, init, init, trim, trim)
        .addStatement("return")
        .endControlFlow();
    code.addStatement("$T $N = $N.split($S, $L)", tokens.type, tokens, input, "\\s+", -1);
    code.addStatement("$T $N = new $T($N)", sb.type, sb, StringBuilder.class, init);
    code.beginControlFlow("for ($T $N : $N)", STRING, token, tokens);

    code.beginControlFlow("if ($N.length() + $N.length() + 1 > $N)",
        token, sb, maxLineWidth);
    code.beginControlFlow("if ($N.toString().isEmpty())", sb)
        .addStatement("$N.println($N)", printStream, token)
        .endControlFlow();
    code.beginControlFlow("else")
        .addStatement("$N.println($N)", printStream, sb)
        .addStatement("$N.setLength(0)", sb)
        .add("for ($T $N = 0; $N < $N; $N++)\n",
            INT, i, i, continuationIndent, i).indent()
        .addStatement(" $N.append(' ')", sb).unindent()
        .addStatement("$N.append($N)", sb, token)
        .endControlFlow();
    code.endControlFlow();
    code.beginControlFlow("else")
        .add("if ($N.length() > 0 && !$T.isWhitespace($N.charAt($N.length() - 1)))\n",
            sb, Character.class, sb, sb).indent()
        .addStatement("$N.append(' ')", sb).unindent()
        .addStatement("$N.append($N)", sb, token)
        .endControlFlow();
    code.endControlFlow();

    code.add("if ($N.length() > 0)\n", sb).indent()
        .addStatement("$N.println($N)", printStream, sb).unindent();
    return methodBuilder("printWrap")
        .addModifiers(accessModifiers)
        .addCode(code.build())
        .addParameters(Arrays.asList(printStream, continuationIndent, init, input))
        .build();
  }

  private MethodSpec maxLineWidthMethod(Modifier[] accessModifiers) {
    ParameterSpec indentParam = builder(maxLineWidth.type, "chars").build();
    return methodBuilder("maxLineWidth")
        .addParameter(indentParam)
        .addStatement("this.$N = $N", maxLineWidth, indentParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec runBeforeExitMethod(Modifier[] accessModifiers) {
    ParameterSpec param = builder(runBeforeExit.type, runBeforeExit.name).build();
    return methodBuilder("runBeforeExit")
        .addParameter(param)
        .addStatement("this.$N = $N", runBeforeExit, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withMessagesMethod(Modifier[] accessModifiers) {
    ParameterSpec resourceBundleParam = builder(messages.type, "map").build();
    MethodSpec.Builder spec = methodBuilder("withMessages");
    return spec.addParameter(resourceBundleParam)
        .addStatement("this.$N = $N", messages, resourceBundleParam)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withResourceBundleMethod(Modifier[] accessModifiers) {
    ParameterSpec bundle = builder(ResourceBundle.class, "bundle").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add("return withMessages($T.list($N.getKeys()).stream()\n", Collections.class, bundle).indent()
        .addStatement(".collect($T.toMap($T.identity(), $N::getString)))", Collectors.class, Function.class, bundle).unindent();
    return methodBuilder("withResourceBundle").addParameter(bundle)
        .returns(context.generatedClass())
        .addCode(code.build())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec withHelpStreamMethod(Modifier[] accessModifiers) {
    return withPrintStreamMethod("withHelpStream", context, out, accessModifiers);
  }

  private MethodSpec withErrorStreamMethod(Modifier[] accessModifiers) {
    return withPrintStreamMethod("withErrorStream", context, err, accessModifiers);
  }

  private static MethodSpec withPrintStreamMethod(
      String methodName, Context context, FieldSpec stream, Modifier[] accessModifiers) {
    ParameterSpec param = builder(stream.type, stream.name).build();
    return methodBuilder(methodName)
        .addParameter(param)
        .addStatement("this.$N = $N", stream, param)
        .addStatement("return this")
        .returns(context.generatedClass())
        .addModifiers(accessModifiers)
        .build();
  }

  private MethodSpec parseMethod(Modifier[] accessModifiers) {

    ParameterSpec args = builder(Constants.STRING_ARRAY, "args").build();
    ParameterSpec e = builder(RuntimeException.class, "e").build();
    CodeBlock.Builder code = CodeBlock.builder();

    context.helpRequestedType().ifPresent(helpRequestedType ->
        code.add("if ($N.length >= 1 && $S.equals($N[0]))\n", args, "--help", args).indent()
            .addStatement("return new $T()", helpRequestedType)
            .unindent());

    code.beginControlFlow("try")
        .addStatement("return new $T(parse($T.asList($N).iterator()))", context.parsingSuccessType(), Arrays.class, args)
        .endControlFlow();

    code.beginControlFlow("catch ($T $N)", RuntimeException.class, e)
        .addStatement("return new $T($N)",
            context.parsingFailedType(), e)
        .endControlFlow();

    return MethodSpec.methodBuilder("parse").addParameter(args)
        .returns(context.parseResultType())
        .addCode(code.build())
        .addModifiers(accessModifiers)
        .build();
  }

  private CodeBlock handleEndOfOptionParsing(ParameterSpec state, ParameterSpec it, ParameterSpec position, ParameterSpec token) {
    CodeBlock.Builder code = CodeBlock.builder().beginControlFlow("while ($N.hasNext())", it);
    code.addStatement("$N = $N.next()", token, it);
    code.add("if ($N >= $N.$N.size())\n", position, state, parserState.positionalParsersField()).indent()
        .addStatement(throwInvalidOptionStatement(token, "Excess param"))
        .unindent();
    code.addStatement("$N += $N.$N.get($N).read($N)", position, state, parserState.positionalParsersField(), position, token);
    code.endControlFlow(); // end loop
    return code.build();
  }

  private static CodeBlock throwInvalidOptionStatement(ParameterSpec token, String message) {
    return CodeBlock.builder()
        .add("throw new $T($S + $N)", RuntimeException.class,
            message + ": ", token)
        .build();
  }

  private MethodSpec createMethod(Modifier[] accessModifiers) {
    MethodSpec.Builder builder = methodBuilder("create");
    builder.addStatement("return new $T()", context.generatedClass());
    return builder.addModifiers(STATIC)
        .addModifiers(accessModifiers)
        .returns(context.generatedClass())
        .build();
  }

  private CodeBlock javadoc() {
    return CodeBlock.builder().add("Generated by <a href=\"" + PROJECT_URL + "\">jbock " +
        getClass().getPackage().getImplementationVersion() +
        "</a>\n").build();
  }

  private static MethodSpec readOptionArgumentMethod() {
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.add("if ($N.charAt(1) == '-' && $N.indexOf('=') >= 0)\n", token, token).indent()
        .addStatement("return $N.substring($N.indexOf('=') + 1)", token, token).unindent();

    code.add("if ($N.charAt(1) != '-' && $N.length() >= 3)\n", token, token).indent()
        .addStatement("return $N.substring(2)", token).unindent();

    code.add("if (!$N.hasNext())\n", it).indent()
        .addStatement("throw new $T($S + $N)", RuntimeException.class,
            "Missing value after token: ", token)
        .unindent();

    code.addStatement("return $N.next()", it);
    return methodBuilder("readOptionArgument")
        .addCode(code.build())
        .addParameters(asList(token, it))
        .returns(STRING)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private MethodSpec synopsisMethod(Modifier[] accessModifiers) {
    CodeBlock.Builder code = CodeBlock.builder();
    ParameterSpec joiner = builder(StringJoiner.class, "joiner").build();

    code.add("return new $T($S)", StringJoiner.class, " ");

    Map<Boolean, List<Parameter>> partitionedOptions = context.parameters().stream()
        .filter(Parameter::isNotPositional)
        .collect(partitioningBy(Parameter::isRequired));

    List<Parameter> requiredNonpos = partitionedOptions.get(true);
    List<Parameter> optionalNonpos = partitionedOptions.get(false);

    List<Parameter> positional = context.parameters().stream()
        .filter(Parameter::isPositional)
        .collect(toList());

    code.add(".add($S)", context.programName());

    if (!optionalNonpos.isEmpty()) {
      code.add(".add($S)", "[options...]");
    }

    for (Parameter param : requiredNonpos) {
      code.add(addBreaks(".add($T.format($S, $T.$L.names.get(0), $T.$L.name().toLowerCase($T.US)))"),
          String.class, "%s <%s>",
          context.optionType(), param.enumConstant(),
          context.optionType(), param.enumConstant(), Locale.class);
    }

    for (Parameter param : positional) {
      if (param.isOptional()) {
        code.add("$Z.add($S)", "[<" + param.enumConstantLower() + ">]");
      } else if (param.isRequired()) {
        code.add("$Z.add($S)", "<" + param.enumConstantLower() + ">");
      } else if (param.isRepeatable()) {
        code.add("$Z.add($S)", "<" + param.enumConstantLower() + ">...");
      } else {
        throw new AssertionError("all cases handled (positional can't be flag)");
      }
    }

    code.add("$Z.toString();\n", joiner);
    MethodSpec.Builder result = MethodSpec.methodBuilder("synopsis")
        .addCode(code.build())
        .returns(STRING);

    return result.addModifiers(accessModifiers).build();
  }

  private MethodSpec parseOrExitMethod(Modifier[] accessModifiers) {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(context.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.addStatement("$T $N = parse($N)", result.type, result, args);

    code.add("if ($N instanceof $T)\n", result, context.parsingSuccessType()).indent()
        .addStatement("return (($T) $N).getResult()", context.parsingSuccessType(), result)
        .unindent();

    context.helpRequestedType().ifPresent(helpRequestedType -> code
        .beginControlFlow("if ($N instanceof $T)", result, helpRequestedType)
        .addStatement("printOnlineHelp($N)", out)
        .addStatement("$N.flush()", out)
        .addStatement("$N.accept($N)", runBeforeExit, result)
        .addStatement("$T.exit(0)", System.class)
        .endControlFlow());

    code.addStatement("(($T) $N).getError().printStackTrace($N)", context.parsingFailedType(), result, err)
        .addStatement("printOnlineHelp($N)", err)
        .addStatement("$N.println($S + (($T) $N).getError().getMessage())", err, "Error: ", context.parsingFailedType(), result);
    if (context.isHelpParameterEnabled()) {
      code.addStatement("$N.println($S)", err, "Try '--help' for more information.");
    }
    code.addStatement("$N.flush()", err)
        .addStatement("$N.accept($N)", runBeforeExit, result)
        .addStatement("$T.exit($L)", System.class, EXITCODE_ON_ERROR)
        .addStatement("throw new $T()", RuntimeException.class);

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(accessModifiers)
        .returns(context.sourceType())
        .addCode(code.build())
        .build();
  }

  private MethodSpec parseMethodOverloadIterator() {

    ParameterSpec state = builder(context.parserStateType(), "state").build();
    ParameterSpec it = builder(STRING_ITERATOR, "it").build();
    ParameterSpec option = builder(context.optionType(), "option").build();
    ParameterSpec token = builder(STRING, "token").build();
    ParameterSpec position = builder(INT, "position").build();

    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = $L", position.type, position, 0);
    code.addStatement("$T $N = new $T()", state.type, state, state.type);

    // begin parsing loop
    code.beginControlFlow("while ($N.hasNext())", it);

    code.addStatement("$T $N = $N.next()", STRING, token, it);

    if (!context.positionalParams().isEmpty()) {
      code.beginControlFlow("if ($S.equals($N))", "--", token)
          .add(handleEndOfOptionParsing(state, it, position, token))
          .addStatement("return $N.build()", state)
          .endControlFlow();
    }

    if (!context.options().isEmpty()) {
      code.addStatement("$T $N = $N.$N($N)", context.optionType(), option, state, parserState.tryReadOption(), token);
      code.beginControlFlow("if ($N != null)", option)
          .addStatement("$N.$N.get($N).read($N, $N, $N)", state, parserState.parsersField(), option, option, token, it)
          .addStatement("continue")
          .endControlFlow();
    }

    // handle unknown token
    code.add("if (!$N.isEmpty() && $N.charAt(0) == '-')\n", token, token).indent()
        .addStatement(throwInvalidOptionStatement(token, "Invalid option"))
        .unindent();

    code.add("if ($N >= $N.$N.size())\n", position, state, parserState.positionalParsersField()).indent()
        .addStatement(throwInvalidOptionStatement(token, "Excess param"))
        .unindent();

    if (!context.positionalParams().isEmpty()) {
      code.addStatement("$N += $N.$N.get($N).read($N)", position, state, parserState.positionalParsersField(), position, token);
    }

    // end parsing loop
    code.endControlFlow();

    code.addStatement("return $N.build()", state);

    return MethodSpec.methodBuilder("parse")
        .addParameter(it)
        .addCode(code.build())
        .addModifiers(PRIVATE)
        .returns(context.sourceType())
        .build();
  }
}
