package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.Description;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static net.jbock.compiler.Analyser.LONG_NAME;
import static net.jbock.compiler.Analyser.SHORT_NAME;
import static net.jbock.compiler.Names.isFlag;

final class OptionInfo {

  private static final FieldSpec IS_FLAG = FieldSpec.builder(TypeName.BOOLEAN, "flag", PUBLIC, FINAL).build();
  private static final FieldSpec DESCRIPTION = FieldSpec.builder(Analyser.STRING, "description", PUBLIC, FINAL).build();

  private static final String VAL = "[VAL]";
  private static final String DD = "--";
  private static final String DS = "  ";
  private static final String CS = ", ";
  private static final String NL = "\n";

  static TypeSpec define(ExecutableElement constructor, ClassName argumentInfo) {
    ParameterSpec longName = ParameterSpec.builder(LONG_NAME.type, LONG_NAME.name).build();
    ParameterSpec shortName = ParameterSpec.builder(SHORT_NAME.type, SHORT_NAME.name).build();
    ParameterSpec isFlag = ParameterSpec.builder(IS_FLAG.type, IS_FLAG.name).build();
    ParameterSpec description = ParameterSpec.builder(DESCRIPTION.type, DESCRIPTION.name).build();
    TypeSpec.Builder builder = TypeSpec.enumBuilder(argumentInfo);
    boolean needsSuffix = constructor.getParameters().stream()
        .map(OptionInfo::upcase)
        .collect(Collectors.toSet()).size() < constructor.getParameters().size();
    for (int i = 0; i < constructor.getParameters().size(); i++) {
      VariableElement variableElement = constructor.getParameters().get(i);
      Names names = Names.create(variableElement);
      boolean flag = isFlag(variableElement);
      Description d = variableElement.getAnnotation(Description.class);
      String desc = d == null ? "" : d.value();
      String suffix = needsSuffix ? String.format("_%d", i) : "";
      String enumConstant = upcase(variableElement) + suffix;
      builder.addEnumConstant(enumConstant, anonymousClassBuilder("$S, $S, $L, $S",
          names.longName, names.shortName, flag, desc).build());
    }
    return builder.addModifiers(PUBLIC)
        .addFields(Arrays.asList(LONG_NAME, SHORT_NAME, IS_FLAG, DESCRIPTION))
        .addMethod(MethodSpec.methodBuilder("describe")
            .addModifiers(PUBLIC)
            .returns(Analyser.STRING)
            .addCode(describeMethod())
            .build())
        .addMethod(MethodSpec.constructorBuilder()
            .addParameters(Arrays.asList(longName, shortName, isFlag, description))
            .addStatement("this.$N = $N", LONG_NAME, longName)
            .addStatement("this.$N = $N", SHORT_NAME, shortName)
            .addStatement("this.$N = $N", IS_FLAG, isFlag)
            .addStatement("this.$N = $N", DESCRIPTION, description)
            .build())
        .build();
  }

  private static String upcase(VariableElement variableElement) {
    return variableElement.getSimpleName().toString().toUpperCase(Locale.ENGLISH);
  }

  private static CodeBlock describeMethod() {
    ParameterSpec sb = ParameterSpec.builder(StringBuilder.class, "sb").build();
    //@formatter:off
    return CodeBlock.builder()
        .addStatement("$T $N = new $T()", StringBuilder.class, sb, StringBuilder.class)
        .beginControlFlow("if ($N)", IS_FLAG)
          .beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
            .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
            .addStatement("$N.append($S).append($S).append($N)", sb, CS, DD, LONG_NAME)
            .endControlFlow()
          .beginControlFlow("else if ($N != null)", LONG_NAME)
            .addStatement("$N.append($S).append($N)", sb, DD, LONG_NAME)
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
            .endControlFlow()
        .endControlFlow()
        .beginControlFlow("else")
          .beginControlFlow("if ($N != null && $N != null)", LONG_NAME, SHORT_NAME)
            .addStatement("$N.append('-').append($N)", sb, SHORT_NAME)
            .addStatement("$N.append($S).append($S).append($N).append(' ').append($S)", sb, CS, DD, LONG_NAME, VAL)
            .endControlFlow()
          .beginControlFlow("else if ($N != null)", LONG_NAME)
            .addStatement("$N.append($S).append($N).append(' ').append($S)", sb, DD, LONG_NAME, VAL)
            .endControlFlow()
          .beginControlFlow("else")
            .addStatement("$N.append('-').append($N).append(' ').append($S)", sb, SHORT_NAME, VAL)
            .endControlFlow()
        .endControlFlow()
        .addStatement("$N.append($S).append($S)", sb, NL, DS)
        .addStatement("$N.append($N)", sb, DESCRIPTION)
        .addStatement("return $N.toString()", sb)
        .build();
    //@formatter:on
  }

  private OptionInfo() {
    throw new UnsupportedOperationException();
  }
}
