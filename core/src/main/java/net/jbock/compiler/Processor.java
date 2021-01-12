package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableList;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;

public final class Processor extends BasicAnnotationProcessor {

  @Override
  protected Iterable<? extends Step> steps() {
    Elements elements = processingEnv.getElementUtils();
    TypeTool tool = new TypeTool(elements, processingEnv.getTypeUtils());
    Messager messager = processingEnv.getMessager();
    Filer filer = processingEnv.getFiler();
    return ImmutableList.of(
        new CommandProcessingStep(tool, messager, filer, elements),
        new MapperProcessingStep(),
        new ParameterMethodProcessingStep(messager));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
