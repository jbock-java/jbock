package net.jbock.qualifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import net.jbock.compiler.GeneratedTypes;

import java.util.function.Consumer;

import static javax.lang.model.element.Modifier.PRIVATE;

public class ExitHookField {

  private final FieldSpec exitHookField;

  private ExitHookField(FieldSpec exitHookField) {
    this.exitHookField = exitHookField;
  }

  public static ExitHookField create(GeneratedTypes generatedTypes) {
    ParameterizedTypeName consumer = ParameterizedTypeName.get(ClassName.get(Consumer.class),
        generatedTypes.parseResultType());
    ParameterSpec result = ParameterSpec.builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.add(generatedTypes.helpRequestedType()
        .map(helpRequestedType -> CodeBlock.builder()
            .add("$N ->\n", result).indent()
            .add("$T.exit($N instanceof $T ? 0 : 1)", System.class, result, helpRequestedType)
            .unindent().build())
        .orElseGet(() -> CodeBlock.of("$N -> $T.exit(1)", result, System.class)));
    return new ExitHookField(FieldSpec.builder(consumer, "exitHook")
        .addModifiers(PRIVATE)
        .initializer(code.build())
        .build());

  }

  public FieldSpec get() {
    return exitHookField;
  }
}
