package net.jbock.compiler.view;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.compiler.GeneratedTypes;
import net.jbock.qualifier.ExitHookField;
import net.jbock.qualifier.SourceElement;

import javax.inject.Inject;
import java.io.PrintStream;
import java.util.Collections;

import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.ParameterSpec.builder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.compiler.Constants.STRING;
import static net.jbock.compiler.Constants.STRING_ARRAY;
import static net.jbock.compiler.view.GeneratedClass.CONTINUATION_INDENT_USAGE;

class ParseOrExitMethod {

  private final SourceElement sourceElement;
  private final GeneratedTypes generatedTypes;
  private final ExitHookField exitHookField;
  private final FieldSpec err = FieldSpec.builder(PrintStream.class, "err", PRIVATE).build();
  private final FieldSpec programName = FieldSpec.builder(STRING, "programName", PRIVATE, FINAL).build();

  @Inject
  ParseOrExitMethod(
      SourceElement sourceElement,
      GeneratedTypes generatedTypes,
      ExitHookField exitHookField) {
    this.sourceElement = sourceElement;
    this.generatedTypes = generatedTypes;
    this.exitHookField = exitHookField;
  }

  MethodSpec define() {

    ParameterSpec args = builder(STRING_ARRAY, "args").build();
    ParameterSpec result = builder(generatedTypes.parseResultType(), "result").build();
    CodeBlock.Builder code = CodeBlock.builder();

    code.addStatement("$T $N = parse($N)", result.type, result, args);

    code.add("if ($N instanceof $T)\n", result, generatedTypes.parsingSuccessWrapperType()).indent()
        .addStatement("return (($T) $N).$L()",
            generatedTypes.parsingSuccessWrapperType(),
            result,
            sourceElement.resultMethodName())
        .unindent();

    generatedTypes.helpRequestedType().ifPresent(helpRequestedType -> code
        .beginControlFlow("if ($N instanceof $T)", result, helpRequestedType)
        .addStatement("printOnlineHelp()")
        .addStatement("$N.flush()", err)
        .addStatement("$N.accept($N)", exitHookField.get(), result)
        .addStatement("throw new $T($S)", RuntimeException.class, "help requested")
        .endControlFlow());

    code.addStatement("$N.println($S + (($T) $N).getError().getMessage())", err, "Error: ", generatedTypes.parsingFailedType(), result);
    if (sourceElement.helpEnabled()) {
      code.addStatement("printTokens($S, usage())", String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " ")));
    } else {
      code.addStatement("printOnlineHelp()");
    }
    if (sourceElement.helpEnabled()) {
      code.addStatement("$N.println($S + $N + $S)", err, "Try '", programName, " --help' for more information.");
    }
    code.addStatement("$N.flush()", err)
        .addStatement("$N.accept($N)", exitHookField.get(), result)
        .addStatement("throw new $T($S)", RuntimeException.class, "parsing error");

    return methodBuilder("parseOrExit").addParameter(args)
        .addModifiers(sourceElement.accessModifiers())
        .returns(generatedTypes.parseSuccessType())
        .addCode(code.build())
        .build();
  }
}
