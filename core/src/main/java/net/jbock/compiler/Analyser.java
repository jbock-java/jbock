package net.jbock.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.Argument;

import javax.annotation.Generated;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.compiler.LessElements.asType;
import static net.jbock.compiler.Processor.SUFFIX;

class Analyser {

  private static final ClassName STRING = ClassName.get(String.class);
  private static final ParameterizedTypeName STRING_MAP = ParameterizedTypeName.get(
      ClassName.get(Map.class), STRING, STRING);
  private static final TypeName STRING_ARRAY = ArrayTypeName.of(STRING);
  private static final ParameterSpec ARGS = ParameterSpec.builder(STRING_ARRAY, "args")
      .build();

  static TypeSpec analyse(ExecutableElement constructor) {
    return TypeSpec.classBuilder(constructor.getEnclosingElement().getSimpleName() + SUFFIX)
        .addAnnotation(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", Processor.class.getName())
            .build())
        .addMethod(MethodSpec.methodBuilder("parse")
            .addParameter(ARGS)
            .addCode(parsingLogic(constructor))
            .addModifiers(PUBLIC)
            .returns(ClassName.get(asType(constructor.getEnclosingElement())))
            .build())
        .addModifiers(PUBLIC, FINAL)
        .addMethod(MethodSpec.constructorBuilder()
            .addStatement("throw new $T()", UnsupportedOperationException.class)
            .addModifiers(PRIVATE)
            .build())
        .build();
  }

  private static CodeBlock parsingLogic(ExecutableElement constructor) {
    List<? extends VariableElement> parameters = constructor.getParameters();
    Set<String> names = parameters.stream().map(Analyser::name).collect(toSet());
    check(parameters);
    ParameterSpec m = ParameterSpec.builder(STRING_MAP, "m").build();
    ParameterSpec i = ParameterSpec.builder(TypeName.INT, "i").build();
    CodeBlock.Builder builder = CodeBlock.builder().addStatement("$T $N = new $T<>()", STRING_MAP, m, HashMap.class);
    builder.beginControlFlow("if ($N.length % 2 != 0)", ARGS)
        .addStatement("throw new $T($S)", IllegalArgumentException.class, "Even number of arguments expected")
        .endControlFlow();
    builder.beginControlFlow("for (int $N = 0; $N < $N.length; $N += 2)", i, i, ARGS, i)
//        .addStatement()
        .endControlFlow();
    for (VariableElement parameter : parameters) {
      String name = name(parameter);
    }
    builder.addStatement("return null");
    return builder.build();
  }

  private static void check(List<? extends VariableElement> parameters) {
    Set<String> check = new HashSet<>();
    parameters.stream().forEach(p -> {
      String name = name(p);
      if (!TypeName.get(p.asType()).equals(STRING)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Argument must be String: " + name, p);
      }
      if (!check.add(name)) {
        throw new ValidationException(Diagnostic.Kind.ERROR,
            "Duplicate name: " + name, p);
      }
    });
  }

  private static String name(VariableElement parameter) {
    Argument annotation = parameter.getAnnotation(Argument.class);
    if (annotation != null) {
      return annotation.value();
    } else {
      return parameter.getSimpleName().toString();
    }
  }
}
