package net.jbock.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.compiler.Processor.Constructor;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.Option.constructorArgumentsForJavadoc;

final class Analyser {

  static final ClassName STRING = ClassName.get(String.class);

  static final FieldSpec LONG_NAME = FieldSpec.builder(STRING, "longName", PRIVATE, FINAL).build();
  static final FieldSpec SHORT_NAME = FieldSpec.builder(ClassName.get(Character.class),
      "shortName", PRIVATE, FINAL).build();

  static final ParameterizedTypeName STRING_LIST = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();
  private static final FieldSpec otherTokens = FieldSpec.builder(STRING_LIST, "otherTokens", PRIVATE, FINAL)
      .build();

  private final Constructor constructor;

  private final ClassName binderClass;
  private final Option option;
  private final ClassName optionTypeClass;
  private final ClassName keysClass;

  private final MethodSpec read;
  private final MethodSpec readOption;
  private final MethodSpec checkConflict;
  private final MethodSpec readArgument;

  private final FieldSpec longNames;
  private final FieldSpec shortNames;
  private final FieldSpec optMap;
  private final FieldSpec optionType;

  private final TypeName optionMapType;

  static Analyser create(Constructor constructor) {
    return new Analyser(constructor);
  }

  private Analyser(Constructor constructor) {
    this.constructor = constructor;
    this.keysClass = constructor.generatedClass.nestedClass("Names");
    this.binderClass = constructor.generatedClass.nestedClass("Binder");
    this.optionTypeClass = constructor.generatedClass.nestedClass("OptionType");
    this.optionType = FieldSpec.builder(optionTypeClass, "type", PRIVATE, FINAL).build();
    this.option = Option.create(constructor,
        constructor.generatedClass.nestedClass("Option"), optionTypeClass, optionType);
    TypeName soType = ParameterizedTypeName.get(ClassName.get(Map.class),
        STRING, option.optionClass);
    ParameterizedTypeName listOfArgumentType = ParameterizedTypeName.get(
        ClassName.get(List.class), STRING);
    this.optionMapType = ParameterizedTypeName.get(ClassName.get(Map.class),
        option.optionClass, listOfArgumentType);
    this.readArgument = readArgumentMethod();
    this.optMap = FieldSpec.builder(optionMapType, "optMap")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.longNames = FieldSpec.builder(soType, "longNames")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.shortNames = FieldSpec.builder(soType, "shortNames")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.readOption = readOptionMethod(keysClass, longNames, shortNames, option.optionClass);
    this.checkConflict = checkConflictMethod(optionMapType, option.optionClass, optionTypeClass, optionType);
    this.read = readMethod(keysClass, readOption, readArgument, optionMapType,
        option.optionClass, optionType, optionTypeClass, checkConflict);
  }

  private static MethodSpec checkConflictMethod(TypeName optionMapType, ClassName optionClass,
                                                ClassName optionTypeClass, FieldSpec optionType) {
    ParameterSpec optionMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    CodeBlock block = CodeBlock.builder()
        .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.REPEATABLE)
        .addStatement("return")
        .endControlFlow()
        .beginControlFlow("if ($N.containsKey($N))", optionMap, option)
        .addStatement("$T $N = $N.$N == $T.$L ? $S : $S", STRING, message, option, optionType,
            optionTypeClass, OptionType.FLAG, "Duplicate flag", "Conflicting token")
        .addStatement("throw new $T($N + $S + $N)", IllegalArgumentException.class,
            message, ": ", token)
        .endControlFlow()
        .build();
    return MethodSpec.methodBuilder("checkConflict")
        .addParameters(Arrays.asList(optionMap, option, token))
        .addCode(block)
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(constructor.generatedClass)
        .addType(Names.create(option.optionClass, keysClass,
            longNames, shortNames).define())
        .addType(Option.create(constructor, option.optionClass, optionTypeClass, optionType).define())
        .addType(Binder.create(binderClass, option, optMap,
            otherTokens, constructor).define())
        .addType(OptionType.define(optionTypeClass))
        .addAnnotation(generatedAnnotation())
        .addMethod(parseMethod())
        .addMethod(read)
        .addMethod(readOption)
        .addMethod(readArgument)
        .addMethod(checkConflict)
        .addModifiers(PUBLIC, FINAL)
        .build();
  }

  private MethodSpec parseMethod() {
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec rest = ParameterSpec.builder(STRING_LIST, "rest").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optionMap").build();
    ParameterSpec stop = ParameterSpec.builder(TypeName.BOOLEAN, "stop").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    if (constructor.stopword != null) {
      builder.addStatement("$T $N = $L", TypeName.BOOLEAN, stop, false);
    }
    //@formatter:off
    builder.addStatement("$T $N = new $T<>()", otherTokens.type, otherTokens, ArrayList.class)
      .addStatement("$T $N = new $T<>()", rest.type, rest, ArrayList.class)
      .addStatement("$T $N = new $T()", names.type, names, keysClass)
      .addStatement("$T $N = new $T<>($T.class)", optMap.type, optMap, EnumMap.class, option.optionClass)
      .addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS)
      .beginControlFlow("while ($N.hasNext())", it)
        .addStatement("$T $N = $N.next()", STRING, token, it)
        .beginControlFlow("if ($N == null)", token)
          .addStatement("throw new $T($S)", IllegalArgumentException.class, "null token")
          .endControlFlow();
    if (constructor.stopword != null) {
      builder.beginControlFlow("if ($N)", stop)
        .addStatement("$N.add($N)", rest, token)
        .addStatement("continue")
        .endControlFlow()
      .beginControlFlow("if ($N.equals($S))", token, constructor.stopword)
        .addStatement("$N = $L", stop, true)
        .addStatement("continue")
        .endControlFlow();
    }
    builder.addStatement("$N($N, $N, $N, $N, $N)",
              read, token, names, optMap, otherTokens, it)
        .endControlFlow()
      .addStatement("return new $T($N, $N, $N)", binderClass, optMap, otherTokens, rest);
    TypeName originalClass = constructor.enclosingType;
    return MethodSpec.methodBuilder("parse")
        .addParameter(ARGS)
        .addCode(builder.build())
        .addException(IllegalArgumentException.class)
        .addJavadoc("Parses the command line arguments and performs basic validation.\n" +
                "\n" +
                "@param args command line arguments\n" +
                "@throws $T if the input is invalid or ambiguous\n" +
                "@return a binder for constructing {@link $T}\n" +
                "\n" +
                "@see $T#$T($L)\n",
            IllegalArgumentException.class,
            constructor.enclosingType,
            originalClass, originalClass, constructorArgumentsForJavadoc(constructor))
        .returns(binderClass)
        .addModifiers(PUBLIC, STATIC)
        .build();
  }

  private static MethodSpec readOptionMethod(ClassName keysClass,
                                             FieldSpec longNames, FieldSpec shortNames,
                                             ClassName optionClass) {
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec idxe = ParameterSpec.builder(INT, "idxe").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if ($N.length() < 2 || !$N.startsWith($S))", token, token, "-")
          .addStatement("return null")
           .endControlFlow()
        .beginControlFlow("if ($N.startsWith($S))", token, "--")
          .addStatement("$T $N = $N.indexOf('=')", INT, idxe, token)
          .beginControlFlow("if ($N < 0)", idxe)
            .addStatement("return $N.$N.get($N.substring(2))",
                names, longNames, token)
            .endControlFlow()
          .addStatement("return $N.$N.get($N.substring(2, $N))",
              names, longNames, token, idxe)
          .endControlFlow()
        .addStatement("return $N.$N.get($N.substring(1, 2))",
            names, shortNames, token);

    //@formatter:on
    return MethodSpec.methodBuilder("readOption")
        .addParameters(Arrays.asList(names, token))
        .addModifiers(STATIC, PRIVATE)
        .returns(optionClass)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec readArgumentMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec idxe = ParameterSpec.builder(INT, "idxe").build();
    ParameterSpec isLong = ParameterSpec.builder(TypeName.BOOLEAN, "isLong").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if (!$N.startsWith($S))", token, "-")
          .addStatement("throw new AssertionError($S)", "invalid token")
          .endControlFlow()
        .addStatement("$T $N = $N.startsWith($S)", TypeName.BOOLEAN, isLong, token, "--")
        .addStatement("$T $N = $N.indexOf('=')", INT, idxe, token)
        .beginControlFlow("if ($N && $N >= 0)", isLong, idxe)
          .add("// long with equals\n")
          .addStatement("return $N.substring($N + 1)", token, idxe)
          .endControlFlow()
        .beginControlFlow("if (!$N && $N.length() > 2)", isLong, token)
          .add("// attached short\n")
          .addStatement("return $N.substring(2)", token)
          .endControlFlow()
        .beginControlFlow("if (!$N.hasNext())", it)
          .addStatement("throw new $T($S + $N)", IllegalArgumentException.class, "Missing value: ", token)
          .endControlFlow()
        .addStatement("return $N.next()", it);
    //@formatter:on
    return MethodSpec.methodBuilder("readArgument")
        .addParameters(Arrays.asList(token, it))
        .returns(STRING)
        .addCode(builder.build())
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private static MethodSpec readMethod(ClassName keysClass,
                                       MethodSpec readOption,
                                       MethodSpec readArgument,
                                       TypeName optionMapType,
                                       ClassName optionClass,
                                       FieldSpec optionType, ClassName optionTypeClass, MethodSpec checkConflict) {
    ParameterSpec names = ParameterSpec.builder(keysClass, "names").build();
    ParameterSpec optMap = ParameterSpec.builder(optionMapType, "optMap").build();
    ParameterSpec otherTokens = ParameterSpec.builder(STRING_LIST, "otherTokens").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec bucket = ParameterSpec.builder(STRING_LIST, "bucket").build();

    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec option = ParameterSpec.builder(optionClass, "option").build();
    ParameterSpec ignore = ParameterSpec.builder(optionClass, "__").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N($N, $N)", option.type, option, readOption, names, token)
        .beginControlFlow("if ($N == null)", option)
          .addStatement("$N.add($N)", otherTokens, token)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$N($N, $N, $N)", checkConflict, optMap, option, token)
        .addStatement("$T $N = $N.computeIfAbsent($N, $N -> new $T<>())",
            bucket.type, bucket, optMap, option, ignore, ArrayList.class)
        .beginControlFlow("if ($N.$N == $T.$L)", option, optionType, optionTypeClass, OptionType.FLAG)
          .add("// add some non-null string to represent the flag\n")
          .addStatement("$N.add($S)", bucket, "t")
          .addStatement("return")
          .endControlFlow()
        .addStatement("$N.add($N($N, $N))", bucket, readArgument, token, it);
    //@formatter:on
    return MethodSpec.methodBuilder("read")
        .addParameters(Arrays.asList(token, names, optMap, otherTokens, it))
        .addModifiers(STATIC, PRIVATE)
        .addCode(builder.build())
        .build();
  }

  private AnnotationSpec generatedAnnotation() {
    return AnnotationSpec.builder(Generated.class)
        .addMember("value", "$S", Processor.class.getName())
        .build();
  }
}
