package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;

class ReadAtFileMethod extends Cached<MethodSpec> {

  private final ReadAtLinesMethod readAtLinesMethod;

  @Inject
  ReadAtFileMethod(ReadAtLinesMethod readAtLinesMethod) {
    this.readAtLinesMethod = readAtLinesMethod;
  }

  @Override
  MethodSpec define() {
    ParameterSpec file = ParameterSpec.builder(STRING, "file").build();
    CodeBlock.Builder code = CodeBlock.builder();
    ParameterSpec path = ParameterSpec.builder(Path.class, "path").build();
    ParameterSpec lines = ParameterSpec.builder(LIST_OF_STRING, "lines").build();
    code.addStatement("$T $N = $T.get($N)", Path.class, path, Paths.class, file);
    code.addStatement("$T $N = $T.readAllLines($N)", LIST_OF_STRING, lines, Files.class, path);
    code.addStatement("return $N($N)", readAtLinesMethod.get(), lines);
    return MethodSpec.methodBuilder("readAtFile")
        .returns(LIST_OF_STRING)
        .addParameter(file)
        .addCode(code.build())
        .addException(IOException.class)
        .build();
  }
}
