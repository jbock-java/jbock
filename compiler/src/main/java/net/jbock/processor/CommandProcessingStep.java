package net.jbock.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import io.jbock.util.Either;
import io.jbock.util.LeftOptional;
import net.jbock.Command;
import net.jbock.common.Annotations;
import net.jbock.common.OperationMode;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.validate.DaggerValidateComponent;
import net.jbock.validate.ValidateComponent;
import net.jbock.validate.ValidateModule;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.List;
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
    private final SafeElements elements;
    private final SourceFileGenerator sourceFileGenerator;

    @Inject
    CommandProcessingStep(
            TypeTool tool,
            Messager messager,
            Util util,
            Filer filer,
            OperationMode operationMode,
            Types types,
            SafeElements elements,
            SourceFileGenerator sourceFileGenerator) {
        this.tool = tool;
        this.messager = messager;
        this.util = util;
        this.filer = filer;
        this.operationMode = operationMode;
        this.types = types;
        this.elements = elements;
        this.sourceFileGenerator = sourceFileGenerator;
    }

    @Override
    public Set<String> annotations() {
        return Stream.of(Command.class)
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
        elementsByAnnotation.forEach((annotationName, element) ->
                ElementFilter.typesIn(List.of(element)).stream()
                        .map(this::validateSourceElement)
                        .forEach(either -> either.accept(this::printFailures, this::processSourceElement)));
        return Set.of();
    }

    private void processSourceElement(SourceElement sourceElement) {
        ValidateComponent component = DaggerValidateComponent.builder()
                .sourceElement(sourceElement)
                .tool(tool)
                .util(util)
                .filer(filer)
                .messager(messager)
                .operationMode(operationMode)
                .module(new ValidateModule(types, elements))
                .create();
        component.processor().generate()
                .accept(this::printFailures, type ->
                        sourceFileGenerator.write(sourceElement, type));
    }

    private Either<List<ValidationFailure>, SourceElement> validateSourceElement(
            TypeElement element) {
        return util.commonTypeChecks(element)
                .or(() -> util.checkNoDuplicateAnnotations(element,
                        Annotations.typeLevelAnnotations()))
                .map(s -> new ValidationFailure(s, element))
                .map(List::of)
                .map(LeftOptional::of)
                .orElse(LeftOptional.empty())
                .orElseRight(() -> SourceElement.create(element));
    }

    private void printFailures(List<ValidationFailure> failures) {
        for (ValidationFailure failure : failures) {
            messager.printMessage(ERROR, failure.message(), failure.about());
        }
    }
}
