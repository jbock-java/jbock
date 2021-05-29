package net.jbock.compiler;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import net.jbock.Command;
import net.jbock.SuperCommand;
import net.jbock.common.Annotations;
import net.jbock.common.OperationMode;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.validate.CommandComponent;
import net.jbock.validate.CommandModule;
import net.jbock.validate.DaggerCommandComponent;
import net.jbock.validate.SourceFileGenerator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.tools.Diagnostic.Kind.ERROR;

@ProcessorScope
public class CommandProcessingStep implements BasicAnnotationProcessor.Step {

  private final TypeTool tool;
  private final Messager messager;
  private final Util util;
  private final Filer filer;
  private final OperationMode operationMode;
  private final Types types;
  private final Elements elements;

  @Inject
  CommandProcessingStep(
      TypeTool tool,
      Messager messager,
      Util util,
      Filer filer,
      OperationMode operationMode,
      Types types,
      Elements elements) {
    this.tool = tool;
    this.messager = messager;
    this.util = util;
    this.filer = filer;
    this.operationMode = operationMode;
    this.types = types;
    this.elements = elements;
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
      ElementFilter.typesIn(List.of(element)).stream()
          .map(typeElement -> validateSourceElement(typeElement, parserFlavour))
          .forEach(either -> either.accept(this::printFailures, this::processSourceElement));
    });
    return Set.of();
  }

  private void processSourceElement(SourceElement sourceElement) {
    CommandComponent component = DaggerCommandComponent.builder()
        .sourceElement(sourceElement)
        .tool(tool)
        .util(util)
        .filer(filer)
        .messager(messager)
        .operationMode(operationMode)
        .module(new CommandModule(types, elements))
        .create();
    SourceFileGenerator sourceFileGenerator = component.sourceFileGenerator();
    component.processor().generate()
        .accept(this::printFailures, sourceFileGenerator::write);
  }

  private Either<List<ValidationFailure>, SourceElement> validateSourceElement(
      TypeElement element,
      ParserFlavour parserFlavour) {
    Optional<List<ValidationFailure>> failureList = util.commonTypeChecks(element)
        .or(() -> util.assertNoDuplicateAnnotations(element,
            Annotations.typeLevelAnnotations()))
        .map(s -> new ValidationFailure(s, element))
        .map(List::of);
    return Either.unbalancedLeft(failureList)
        .orElseRight(() -> SourceElement.create(element, parserFlavour));
  }

  private void printFailures(List<ValidationFailure> failures) {
    for (ValidationFailure failure : failures) {
      messager.printMessage(ERROR, failure.message(), failure.about());
    }
  }
}
