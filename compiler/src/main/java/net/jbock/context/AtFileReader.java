package net.jbock.context;

import com.squareup.javapoet.TypeSpec;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;

public class AtFileReader extends Cached<TypeSpec> {

  private final SourceElement sourceElement;
  private final GeneratedAnnotation generatedAnnotation;
  private final ReadTokenFromAtFileMethod readTokenFromAtFileMethod;
  private final ReadAtLinesMethod readAtLinesMethod;
  private final ReadAtFileMethod readAtFileMethod;

  @Inject
  AtFileReader(
      SourceElement sourceElement,
      GeneratedAnnotation generatedAnnotation,
      ReadTokenFromAtFileMethod readTokenFromAtFileMethod,
      ReadAtLinesMethod readAtLinesMethod,
      ReadAtFileMethod readAtFileMethod) {
    this.sourceElement = sourceElement;
    this.generatedAnnotation = generatedAnnotation;
    this.readTokenFromAtFileMethod = readTokenFromAtFileMethod;
    this.readAtLinesMethod = readAtLinesMethod;
    this.readAtFileMethod = readAtFileMethod;
  }

  @Override
  TypeSpec define() {
    return TypeSpec.classBuilder(sourceElement.atFileReaderType())
        .addMethod(readAtFileMethod.get())
        .addMethod(readAtLinesMethod.get())
        .addMethod(readTokenFromAtFileMethod.get())
        .addAnnotation(generatedAnnotation.get())
        .build();
  }
}
