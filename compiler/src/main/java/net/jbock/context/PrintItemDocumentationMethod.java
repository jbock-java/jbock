package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class PrintItemDocumentationMethod extends Cached<MethodSpec> {

  private final AllItems allItems;
  private final SourceElement sourceElement;
  private final CommonFields commonFields;
  private final MakeLinesMethod makeLinesMethod;

  @Inject
  PrintItemDocumentationMethod(
      AllItems allItems,
      SourceElement sourceElement,
      CommonFields commonFields,
      MakeLinesMethod makeLinesMethod) {
    this.allItems = allItems;
    this.sourceElement = sourceElement;
    this.commonFields = commonFields;
    this.makeLinesMethod = makeLinesMethod;
  }

  @Override
  MethodSpec define() {
    ParameterSpec descriptionKey = builder(STRING, "descriptionKey").build();
    ParameterSpec message = builder(STRING, "message").build();
    ParameterSpec item = builder(sourceElement.itemType(), "item").build();
    ParameterSpec names = builder(STRING, "names").build();
    ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();
    ParameterSpec indent = builder(STRING, "indent").build();
    ParameterSpec s = builder(STRING, "s").build();
    CodeBlock.Builder code = CodeBlock.builder();
    if (allItems.anyDescriptionKeys()) {
      code.addStatement("$T $N = $N.isEmpty() ? null : $N.get($N)",
          message.type, message, descriptionKey, commonFields.messages(), descriptionKey);
    }

    code.addStatement("$T $N = new $T<>()", tokens.type, tokens, ArrayList.class);
    code.addStatement("$N.add($N)", tokens, names);
    if (allItems.anyDescriptionKeys()) {
      code.addStatement(CodeBlock.builder().add("$N.addAll($T.ofNullable($N)\n",
          tokens, Optional.class, message).indent()
          .add(".map($T::trim)\n", STRING)
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".map($T::asList)\n", Arrays.class)
          .add(".orElseGet(() -> $T.stream($N.description)\n", Arrays.class, item).indent()
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".flatMap($T::stream)\n", Arrays.class)
          .add(".collect($T.toList())))", Collectors.class)
          .unindent()
          .unindent()
          .build());
    } else {
      code.addStatement(CodeBlock.builder()
          .add("$T.stream($N.description)\n", Arrays.class, item).indent()
          .add(".map($N -> $N.split($S, $L))\n", s, s, "\\s+", -1)
          .add(".flatMap($T::stream)\n", Arrays.class)
          .add(".forEach($N::add)", tokens)
          .unindent()
          .build());
    }
    code.addStatement("$N($N, $N).forEach($N::println)", makeLinesMethod.get(),
        indent, tokens, commonFields.err());
    MethodSpec.Builder spec = methodBuilder("printItemDocumentation")
        .addParameter(item)
        .addParameter(names)
        .addParameter(indent)
        .addModifiers(PRIVATE)
        .addCode(code.build());
    if (allItems.anyDescriptionKeys()) {
      spec.addParameter(descriptionKey);
    }
    return spec.build();
  }
}
