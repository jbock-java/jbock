package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.processor.SourceElement;
import net.jbock.util.StringConverter;

import javax.inject.Inject;
import java.io.File;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class ConverterFileExists {

  private final SourceElement sourceElement;

  @Inject
  ConverterFileExists(SourceElement sourceElement) {
    this.sourceElement = sourceElement;
  }

  TypeSpec define() {
    return TypeSpec.classBuilder(sourceElement.converterFileExistsType())
        .addMethod(convertMethod())
        .superclass(ParameterizedTypeName.get(StringConverter.class, File.class))
        .addModifiers(PRIVATE, STATIC)
        .build();
  }

  private MethodSpec convertMethod() {
    ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
    ParameterSpec file = ParameterSpec.builder(File.class, "file").build();
    CodeBlock.Builder code = CodeBlock.builder();
    code.addStatement("$T $N = new $T($N)", File.class, file, File.class, token);
    code.add("if (!$N.exists())\n", file).indent()
        .addStatement("throw new $T($S + $N)", IllegalStateException.class,
            "File does not exist: ", token)
        .unindent();
    code.add("if (!$N.isFile())\n", file).indent()
        .addStatement("throw new $T($S + $N)", IllegalStateException.class,
            "Not a file: ", token)
        .unindent();
    code.addStatement("return $N", file);
    MethodSpec.Builder spec = MethodSpec.methodBuilder("convert");
    spec.addAnnotation(Override.class);
    spec.addCode(code.build());
    spec.addParameter(token);
    spec.addModifiers(PROTECTED);
    spec.returns(File.class);
    return spec.build();
  }
}
