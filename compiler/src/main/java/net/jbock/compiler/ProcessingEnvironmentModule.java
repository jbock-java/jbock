package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

@Module
public interface ProcessingEnvironmentModule {

  @ProcessorScope
  @Provides
  static Messager messager(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getMessager();
  }

  @ProcessorScope
  @Provides
  static Filer filer(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getFiler();
  }

  @ProcessorScope
  @Provides
  static Elements elements(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getElementUtils();
  }

  @ProcessorScope
  @Provides
  static Types types(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getTypeUtils();
  }

  @ProcessorScope
  @Provides
  static TypeTool tool(ProcessingEnvironment processingEnv) {
    return new TypeTool(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
  }

  @ProcessorScope
  @Provides
  static Util util(Types types, TypeTool tool) {
    return new Util(types, tool);
  }
}
