package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;

@Module
public interface ProcessingEnvironmentModule {

  @Reusable
  @Provides
  static Messager messager(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getMessager();
  }

  @Reusable
  @Provides
  static Filer filer(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getFiler();
  }

  @Reusable
  @Provides
  static Elements elements(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getElementUtils();
  }

  @Reusable
  @Provides
  static Types types(ProcessingEnvironment processingEnvironment) {
    return processingEnvironment.getTypeUtils();
  }

  @Reusable
  @Provides
  static TypeTool tool(ProcessingEnvironment processingEnv) {
    return new TypeTool(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
  }

  @Reusable
  @Provides
  static List<? extends BasicAnnotationProcessor.Step> steps(
      CommandProcessingStep commandProcessingStep,
      MapperProcessingStep mapperProcessingStep,
      ParameterMethodProcessingStep parameterMethodProcessingStep) {
    return ImmutableList.of(
        commandProcessingStep,
        mapperProcessingStep,
        parameterMethodProcessingStep);
  }
}
