package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Command;
import net.jbock.SuperCommand;
import net.jbock.compiler.command.CommandComponent;
import net.jbock.compiler.command.DaggerCommandComponent;
import net.jbock.compiler.command.SourceFileGenerator;
import net.jbock.convert.Util;
import net.jbock.qualifier.SourceElement;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandProcessingStep implements BasicAnnotationProcessor.Step {

  private final TypeTool tool;
  private final Messager messager;
  private final Util util;
  private final Filer filer;
  private final OperationMode operationMode;

  @Inject
  CommandProcessingStep(
      TypeTool tool,
      Messager messager,
      Util util,
      Filer filer,
      OperationMode operationMode) {
    this.tool = tool;
    this.messager = messager;
    this.util = util;
    this.filer = filer;
    this.operationMode = operationMode;
  }

  @Override
  public Set<String> annotations() {
    return Stream.of(Command.class, SuperCommand.class)
        .map(Class::getCanonicalName)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
    elementsByAnnotation.forEach((annotationName, element) -> {
      ParserFlavour parserFlavour = ParserFlavour.forAnnotationName(annotationName);
      ElementFilter.typesIn(Collections.singletonList(element))
          .forEach(typeElement -> {
            SourceElement sourceElement = SourceElement.create(typeElement, parserFlavour);
            processSourceElement(sourceElement);
          });
    });
    return Collections.emptySet();
  }

  private void processSourceElement(SourceElement sourceElement) {
    CommandComponent component = DaggerCommandComponent.builder()
        .sourceElement(sourceElement)
        .tool(tool)
        .util(util)
        .filer(filer)
        .messager(messager)
        .operationMode(operationMode)
        .create();
    SourceFileGenerator sourceFileGenerator = component.sourceFileGenerator();
    component.processor().generate()
        .accept(this::printFailures, sourceFileGenerator::write);
  }

  private void printFailures(java.util.List<ValidationFailure> failures) {
    for (ValidationFailure failure : failures) {
      messager.printMessage(Diagnostic.Kind.ERROR, failure.message(), failure.about());
    }
  }
}
