package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@Module
public interface ProcessingEnvironmentModule {

  @Provides
  static Messager messager(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getMessager();
  }

  @Provides
  static Filer filer(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getFiler();
  }

  @Provides
  static Elements elements(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getElementUtils();
  }

  @Provides
  static Types types(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getTypeUtils();
  }

  @Provides
  static TypeTool tool(ProcessingEnvironment processingEnv) {
    return new TypeTool(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
  }
}
