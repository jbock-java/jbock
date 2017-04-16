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

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.squareup.javapoet.TypeName.INT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.compiler.LessElements.asType;
import static net.jbock.compiler.Names.isFlag;

final class Analyser {

  static final ClassName STRING = ClassName.get(String.class);

  static final FieldSpec LONG_NAME = FieldSpec.builder(STRING, "longName", PUBLIC, FINAL).build();
  static final FieldSpec SHORT_NAME = FieldSpec.builder(STRING, "shortName", PUBLIC, FINAL).build();
  static final FieldSpec IS_FLAG = FieldSpec.builder(TypeName.BOOLEAN, "flag", PUBLIC, FINAL).build();


  static final ParameterizedTypeName STRING_LIST = ParameterizedTypeName.get(
      ClassName.get(List.class), STRING);

  private static final ParameterizedTypeName STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);
  private static final ParameterizedTypeName STRING_SET = ParameterizedTypeName.get(
      ClassName.get(Set.class), STRING);
  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final TypeName STRING_ITERATOR = ParameterizedTypeName.get(ClassName.get(Iterator.class), STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();
  private static final ClassName LIST = ClassName.get(List.class);

  private final ExecutableElement constructor;
  private final ClassName generatedClass;
  private final MethodSpec getParam;
  private final MethodSpec getBool;
  private final MethodSpec addNextOm;
  private final MethodSpec whichOption;
  private final TypeName entryType;

  private final FieldSpec SHORT_FLAGS;
  private final FieldSpec LONG_FLAGS;
  private final FieldSpec LONG_NAMES;
  private final FieldSpec SHORT_NAMES;
  private final FieldSpec optMap;

//  private final MethodSpec addNext;

/*
  private static final FieldSpec longOptions = FieldSpec.builder(STRING_MAP, "longOptions", PRIVATE, FINAL)
      .build();

  private static final FieldSpec shortOptions = FieldSpec.builder(STRING_MAP, "shortOptions", PRIVATE, FINAL)
      .build();
*/

  private static final FieldSpec trash = FieldSpec.builder(STRING_LIST, "trash", PRIVATE, FINAL)
      .build();

  private final ClassName argumentInfo;
  private final ClassName optionInfo;
  private final TypeName omType;
  private final TypeName moType;

  Analyser(ExecutableElement constructor, ClassName generatedClass) {
    this.constructor = constructor;
    this.generatedClass = generatedClass;
    this.argumentInfo = generatedClass.nestedClass("Argument");
    this.optionInfo = generatedClass.nestedClass("Option");
    this.omType = ParameterizedTypeName.get(ClassName.get(Map.class),
        optionInfo, STRING);
    this.optMap = FieldSpec.builder(omType, "optMap")
        .addModifiers(PRIVATE, FINAL)
        .build();
    this.moType = ParameterizedTypeName.get(ClassName.get(Map.class), STRING, optionInfo);
    this.SHORT_FLAGS = FieldSpec.builder(moType, "SHORT_FLAGS")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
    this.LONG_FLAGS = FieldSpec.builder(moType, "LONG_FLAGS")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
    this.LONG_NAMES = FieldSpec.builder(moType, "LONG_NAMES")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
    this.SHORT_NAMES = FieldSpec.builder(moType, "SHORT_NAMES")
        .addModifiers(PRIVATE, STATIC, FINAL)
        .build();
//    this.addNext = addNext(LONG_FLAGS, SHORT_FLAGS, LONG_NAMES, SHORT_NAMES, moType);
    this.entryType = ParameterizedTypeName.get(
        ClassName.get(AbstractMap.Entry.class), optionInfo, STRING);
    this.whichOption = whichOption(LONG_FLAGS, SHORT_FLAGS, LONG_NAMES, SHORT_NAMES, entryType);
    this.addNextOm = addNextOm(whichOption, entryType, omType);
    this.getParam = getParam(optMap, optionInfo);
    this.getBool = getBool(optionInfo, getParam);
  }

  private static MethodSpec getBool(ClassName optionInfo, MethodSpec getParam) {
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("return $N($N) != null", getParam, option);
    return MethodSpec.methodBuilder("getBool")
        .addParameter(option)
        .addCode(builder.build())
        .returns(TypeName.BOOLEAN)
        .addModifiers(PUBLIC)
        .build();
  }

  private static MethodSpec getParam(FieldSpec optMap, ClassName optionInfo) {
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
/*
    ParameterSpec longValue = ParameterSpec.builder(STRING, "longValue").build();
    ParameterSpec shortValue = ParameterSpec.builder(STRING, "shortValue").build();
*/
    CodeBlock.Builder builder = CodeBlock.builder();
    builder.addStatement("return $N.get($N)", optMap, option);
/*
    builder.addStatement("$T $N = null, $N = null", STRING, longValue, shortValue);
    builder.beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
        .addStatement("$N = this.$N.get($N.$N)", longValue, longOptions, option, LONG_NAME)
        .endControlFlow();
    builder.beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
        .addStatement("$N = this.$N.get($N.$N)", shortValue, shortOptions, option, SHORT_NAME)
        .endControlFlow();
    builder.beginControlFlow("if ($N == null)", longValue)
        .addStatement("return $N", shortValue)
        .endControlFlow();
    builder.beginControlFlow("else if ($N == null)", shortValue)
        .addStatement("return $N", longValue)
        .endControlFlow();
    builder.beginControlFlow("else")
        .addStatement("throw new $T($S + $N.$N + $S + $N.$N)", IllegalArgumentException.class,
            "Competing arguments: --", option, LONG_NAME, " versus -", option, SHORT_NAME)
        .endControlFlow();
*/
    return MethodSpec.methodBuilder("param")
        .addParameter(option)
        .addCode(builder.build())
        .returns(STRING)
        .addModifiers(PUBLIC)
        .build();
  }

  TypeSpec analyse() {
    return TypeSpec.classBuilder(generatedClass)
        .addStaticBlock(initializerBlock(moType, optionInfo))
        .addType(ArgumentInfo.create(optionInfo, argumentInfo).define())
        .addType(OptionInfo.create(constructor, optionInfo).define())
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", Processor.class.getName())
            .build())
        .addMethod(MethodSpec.methodBuilder("parse")
            .addCode(bindingCall())
            .addModifiers(PUBLIC)
            .returns(ClassName.get(asType(constructor.getEnclosingElement())))
            .build())
        .addMethod(getParam)
        .addMethod(getBool)
//        .addMethod(addNext)
        .addMethod(addNextOm)
        .addMethod(whichOption)
        .addMethod(MethodSpec.methodBuilder("arguments")
            .addCode(arguments())
            .returns(ParameterizedTypeName.get(LIST, argumentInfo))
            .addModifiers(PUBLIC)
            .build())
        .addMethod(MethodSpec.methodBuilder("trash")
            .addStatement("return $N", trash)
            .returns(trash.type)
            .addModifiers(PUBLIC)
            .build())
        .addModifiers(PUBLIC, FINAL)
        .addFields(Arrays.asList(LONG_FLAGS, SHORT_FLAGS, LONG_NAMES, SHORT_NAMES))
        .addFields(Arrays.asList(trash, optMap))
        .addMethod(privateConstructor())
        .addMethod(MethodSpec.methodBuilder("init")
            .addParameter(ARGS)
            .addCode(factoryMethodBody())
            .returns(generatedClass)
            .addModifiers(PUBLIC, STATIC)
            .build())
        .build();
  }

  private CodeBlock initializerBlock(TypeName moType, ClassName optionInfo) {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec shortFlags = ParameterSpec.builder(moType, "shortFlags")
        .build();
    ParameterSpec longFlags = ParameterSpec.builder(moType, "longFlags")
        .build();
    ParameterSpec longNames = ParameterSpec.builder(moType, "longNames")
        .build();
    ParameterSpec shortNames = ParameterSpec.builder(moType, "shortNames")
        .build();
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option")
        .build();
    builder.addStatement("$T $N = new $T<>()", longFlags.type, longFlags, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortFlags.type, shortFlags, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", longNames.type, longNames, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortNames.type, shortNames, HashMap.class);
    //@formatter:off
    builder.beginControlFlow("for ($T $N : $T.values())", optionInfo, option, optionInfo)
          .beginControlFlow("if ($N.$N)", option, IS_FLAG)
            .beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
              .addStatement("$N.put($N.$N, $N)", shortFlags, option, SHORT_NAME, option)
              .endControlFlow()
            .beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
              .addStatement("$N.put($N.$N, $N)", longFlags, option, LONG_NAME, option)
              .endControlFlow()
            .endControlFlow()
          .beginControlFlow("else")
            .beginControlFlow("if ($N.$N != null)", option, SHORT_NAME)
              .addStatement("$N.put($N.$N, $N)", shortNames, option, SHORT_NAME, option)
              .endControlFlow()
            .beginControlFlow("if ($N.$N != null)", option, LONG_NAME)
              .addStatement("$N.put($N.$N, $N)", longNames, option, LONG_NAME, option)
              .endControlFlow()
            .endControlFlow()
        .endControlFlow();
    //@formatter:on
    builder.addStatement("$N = $T.unmodifiableMap($N)", LONG_FLAGS, Collections.class, longFlags);
    builder.addStatement("$N = $T.unmodifiableMap($N)", SHORT_FLAGS, Collections.class, shortFlags);
    builder.addStatement("$N = $T.unmodifiableMap($N)", LONG_NAMES, Collections.class, longNames);
    builder.addStatement("$N = $T.unmodifiableMap($N)", SHORT_NAMES, Collections.class, shortNames);
    return builder.build();
  }

  private CodeBlock factoryMethodBody() {
    CodeBlock.Builder builder = CodeBlock.builder();

/*
    ParameterSpec longOpts = ParameterSpec.builder(STRING_MAP, "longOpts").build();
    ParameterSpec shortOpts = ParameterSpec.builder(STRING_MAP, "shortOpts").build();
*/
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec om = ParameterSpec.builder(omType, "optionMap").build();

/*
    builder.addStatement("$T $N = new $T<>()", longOpts.type, longOpts, HashMap.class);
    builder.addStatement("$T $N = new $T<>()", shortOpts.type, shortOpts, HashMap.class);
*/
    builder.addStatement("$T $N = new $T<>()", trash.type, trash, ArrayList.class);
    builder.addStatement("$T $N = new $T<>($T.class)", om.type, om, EnumMap.class, optionInfo);

    // read args
    builder.addStatement("$T $N = $T.stream($N).iterator()", it.type, it, Arrays.class, ARGS);
    builder.beginControlFlow("while ($N.hasNext())", it)
//        .addStatement("$N($N, $N, $N, $N)", addNextOm, longOpts, shortOpts, trash, it)
        .addStatement("$N($N, $N, $N)", addNextOm, om, trash, it)
        .endControlFlow();
    builder.addStatement("return new $T($N, $N)", generatedClass, trash, om);
    return builder.build();
  }

  private static MethodSpec whichOption(FieldSpec LONG_FLAGS, FieldSpec SHORT_FLAGS,
                                        FieldSpec LONG_NAMES, FieldSpec SHORT_NAMES,
                                        TypeName entryType) {
    ParameterSpec st = ParameterSpec.builder(STRING, "st").build();
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec ie = ParameterSpec.builder(INT, "ie").build();
    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .beginControlFlow("if ($N.startsWith($S))", s, "--")
          .addStatement("$T $N = $N.substring(2)", STRING, st, s)
          .addStatement("$T $N = $N.indexOf('=')", INT, ie, st)
          .beginControlFlow("if ($N < 0 && $N.containsKey($N))", ie, LONG_FLAGS, st)
            .addStatement("return new $T<>($N.get($N), $N)",
                SimpleImmutableEntry.class, LONG_FLAGS, st, s)
            .endControlFlow()
          .beginControlFlow("if ($N >= 0 && $N.containsKey($N.substring(0, $N)))",
              ie, LONG_NAMES, st, ie)
            .addStatement("return new $T<>($N.get($N.substring(0, $N)), $N)",
                SimpleImmutableEntry.class, LONG_NAMES, st, ie, s)
            .endControlFlow()
          .addStatement("return null")
          .endControlFlow();

        builder.beginControlFlow("if ($N.startsWith($S))", s, "-")
          .addStatement("$T $N = $N.substring(1)", STRING, st, s)
          .beginControlFlow("if ($N.isEmpty())", st)
            .addStatement("return null")
            .endControlFlow()
          .beginControlFlow("if ($N.length() == 1 && $N.containsKey($N))", st, SHORT_FLAGS, st)
            .addStatement("return new $T<>($N.get($N), $N)",
                SimpleImmutableEntry.class, SHORT_FLAGS, st, s)
            .endControlFlow()
          .beginControlFlow("if ($N.containsKey($N.substring(0, 1)))", SHORT_NAMES, st)
            .addStatement("return new $T<>($N.get($N.substring(0, 1)), $N)",
                SimpleImmutableEntry.class, SHORT_NAMES, st, s)
            .endControlFlow()
          .endControlFlow();

    builder.addStatement("return null");

    //@formatter:on
    return MethodSpec.methodBuilder("whichOption")
        .addParameter(s)
        .addModifiers(STATIC, PRIVATE)
        .returns(entryType)
        .addCode(builder.build())
        .build();
  }

  private static MethodSpec addNextOm(MethodSpec whichOption, TypeName entryType, TypeName omType) {
    ParameterSpec om = ParameterSpec.builder(omType, "optionMap").build();
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();

    ParameterSpec s = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec entry = ParameterSpec.builder(entryType, "e").build();
    ParameterSpec ie = ParameterSpec.builder(INT, "ie").build();
    //@formatter:off
    CodeBlock block = CodeBlock.builder()
        .addStatement("$T $N = $N.next()", STRING, s, it)
        .addStatement("$T $N = $N($N)", entry.type, entry, whichOption, s)
        .beginControlFlow("if ($N == null)", entry)
          .addStatement("$N.add($N)", trash, s)
          .addStatement("return")
          .endControlFlow()
        .beginControlFlow("if ($N.getKey().flag)", entry)
          .addStatement("$N.put($N.getKey(), $N.getValue())", om, entry, entry)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$T $N = $N.getValue().indexOf('=')", INT, ie, entry)
        .beginControlFlow("if ($N < 0)", ie)
          .beginControlFlow("if (!$N.hasNext())", it)
            .addStatement("$N.add($N)", trash, s)
            .addStatement("return")
            .endControlFlow()
          .addStatement("$N.put($N.getKey(), $N.next())", om, entry, it)
          .addStatement("return")
          .endControlFlow()
        .addStatement("$N.put($N.getKey(), $N.getValue().substring($N + 1))", om, entry, entry, ie)
        .build();
    //@formatter:on
    return MethodSpec.methodBuilder("addNextOm")
        .addParameters(Arrays.asList(om, trash, it))
        .addModifiers(STATIC, PRIVATE)
        .addCode(block)
        .build();
  }

/*
  private static MethodSpec addNext(FieldSpec LONG_FLAGS, FieldSpec SHORT_FLAGS,
                                    FieldSpec LONG_NAMES, FieldSpec SHORT_NAMES,
                                    TypeName moType) {
    ParameterSpec longOpts = ParameterSpec.builder(STRING_MAP, "longOpts").build();
    ParameterSpec shortOpts = ParameterSpec.builder(STRING_MAP, "shortOpts").build();
    ParameterSpec trash = ParameterSpec.builder(STRING_LIST, "trash").build();
    ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();

    ParameterSpec st = ParameterSpec.builder(STRING, "st").build();
    ParameterSpec set = ParameterSpec.builder(moType, "set").build();
    ParameterSpec m = ParameterSpec.builder(STRING_MAP, "m").build();

    //@formatter:off
    CodeBlock.Builder builder = CodeBlock.builder()
        .addStatement("$T $N = $N.next()", STRING, s, it)
        .addStatement("$T $N", st.type, st)
        .addStatement("$T $N", set.type, set)
        .addStatement("$T $N", m.type, m)
        .beginControlFlow("if ($N.startsWith($S))", s, "--")
          .addStatement("$N = $N.substring(2)", st, s)
          .beginControlFlow("if ($N.containsKey($N))", LONG_FLAGS, st)
            .addStatement("$N.put($N, $S)", longOpts, st, Boolean.TRUE.toString())
            .addStatement("return")
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N = $N", set, LONG_NAMES)
            .addStatement("$N = $N", m, longOpts)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else if ($N.startsWith($S))", s, "-")
          .addStatement("$N = $N.substring(1)", st, s)
          .beginControlFlow("if ($N.containsKey($N))", SHORT_FLAGS, st)
            .addStatement("$N.put($N, $S)", shortOpts, st, Boolean.TRUE.toString())
            .addStatement("return")
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N = $N", set, SHORT_NAMES)
            .addStatement("$N = $N", m, shortOpts)
            .endControlFlow()
          .endControlFlow()
        .beginControlFlow("else")
          .addStatement("$N.add($N)", trash, s)
          .addStatement("return")
          .endControlFlow();

    return MethodSpec.methodBuilder("addNext")
        .addParameters(Arrays.asList(longOpts, shortOpts, trash, it))
        .addCode(builder
            .beginControlFlow("if (!$N.containsKey($N))", set, st)
              .addStatement("$N.add($N)", trash, s)
              .addStatement("return")
              .endControlFlow()
            .beginControlFlow("if ($N.containsKey($N))", m, st)
              .addStatement("throw new $T($S + $N)",
                  IllegalArgumentException.class, "Duplicate argument: ", st)
              .endControlFlow()
            .beginControlFlow("if (!$N.hasNext())", it)
              .addStatement("$N.add($N)", trash, s)
              .addStatement("return")
              .endControlFlow()
            .addStatement("$N.put($N, $N.next())", m, st, it)
            .build())
        .addModifiers(STATIC, PRIVATE)
        .build();
    //@formatter:on
  }
*/

  private MethodSpec privateConstructor() {
//    ParameterSpec ln = ParameterSpec.builder(STRING_MAP, longOptions.name).build();
//    ParameterSpec sn = ParameterSpec.builder(STRING_MAP, shortOptions.name).build();
    ParameterSpec tr = ParameterSpec.builder(STRING_LIST, trash.name).build();
    ParameterSpec om = ParameterSpec.builder(optMap.type, optMap.name).build();
    return MethodSpec.constructorBuilder()
        .addParameters(Arrays.asList(tr, om))
/*
        .addStatement("this.$N = $T.unmodifiableMap($N)", longOptions, Collections.class, ln)
        .addStatement("this.$N = $T.unmodifiableMap($N)", shortOptions, Collections.class, sn)
*/
        .addStatement("this.$N = $T.unmodifiableList($N)", trash, Collections.class, tr)
        .addStatement("this.$N = $T.unmodifiableMap($N)", optMap, Collections.class, om)
        .addModifiers(PRIVATE)
        .build();
  }

  private CodeBlock bindingCall() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec options = ParameterSpec.builder(ArrayTypeName.of(optionInfo), "options").build();
    builder.addStatement("$T $N = $T.values()",
        options.type, options, optionInfo);
    builder.add("return new $T(\n    ", ClassName.get(constructor.getEnclosingElement().asType()));
    for (int j = 0; j < constructor.getParameters().size(); j++) {
      VariableElement variableElement = constructor.getParameters().get(j);
      if (j > 0) {
        builder.add(",\n    ");
      }
      MethodSpec op = isFlag(variableElement) ? getBool : getParam;
      builder.add("$N($N[$L])", op, options, j);
    }
    builder.add(");\n");
    return builder.build();
  }

  private CodeBlock arguments() {
    CodeBlock.Builder builder = CodeBlock.builder();
    ParameterSpec entries = ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), argumentInfo),
        "arguments").build();
    builder.addStatement("$T $N = new $T<>($L)", entries.type,
        entries, ArrayList.class, constructor.getParameters().size());
    ParameterSpec option = ParameterSpec.builder(optionInfo, "option").build();
    builder.beginControlFlow("for ($T $N : $T.values())", option.type, option, optionInfo)
        .addStatement("$N.add(new $T($N, $N($N)))", entries,
            argumentInfo, option, getParam, option)
        .endControlFlow();
    builder.addStatement("return $N", entries);
    return builder.build();
  }

  private static CodeBlock options(ClassName optionInfo) {
    return CodeBlock.builder()
        .addStatement("return $T.asList($T.values())", Arrays.class, optionInfo)
        .build();
  }
}
