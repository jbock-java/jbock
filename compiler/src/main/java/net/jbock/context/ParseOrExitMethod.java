package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.contrib.StandardErrorHandler;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.STRING_ARRAY;

@ContextScope
public class ParseOrExitMethod {

  private final SourceElement sourceElement;
  private final GeneratedTypes generatedTypes;
  private final ParseMethod parseMethod;

  @Inject
  ParseOrExitMethod(
      SourceElement sourceElement,
      GeneratedTypes generatedTypes,
      ParseMethod parseMethod) {
    this.sourceElement = sourceElement;
    this.generatedTypes = generatedTypes;
    this.parseMethod = parseMethod;
  }

  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec notSuccess = builder(generatedTypes.parseResultType(), "notSuccess").build();

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(sourceElement.accessModifiers())
        .returns(generatedTypes.parseSuccessType())
        .addCode(CodeBlock.builder()
            .add("return $N($N)", parseMethod.get(), args)
            .add(".orElseThrow($N ->\n", notSuccess).indent()
            .add("$T.builder($N).build().handle());\n", StandardErrorHandler.class, notSuccess).unindent()
            .build())
        .build();
  }
}
