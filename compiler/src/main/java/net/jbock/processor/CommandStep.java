package net.jbock.processor;

import io.jbock.javapoet.JavaFile;
import io.jbock.javapoet.TypeSpec;
import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.Command;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.validate.CommandProcessor;
import net.jbock.validate.ValidateComponent;
import net.jbock.writing.ContextComponent;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jbock.auto.common.BasicAnnotationProcessor.Step;
import static io.jbock.util.Either.right;

/**
 * This step handles the {@link Command} annotation.
 * It performs validation and source generation.
 *
 * @see ProcessorScope
 */
@ProcessorScope
class CommandStep implements Step {

    private final Messager messager;
    private final Util util;
    private final SafeTypes types;
    private final SafeElements elements;
    private final SourceFileGenerator sourceFileGenerator;

    @Inject
    CommandStep(
            Messager messager,
            Util util,
            SafeTypes types,
            SafeElements elements,
            SourceFileGenerator sourceFileGenerator) {
        this.messager = messager;
        this.util = util;
        this.types = types;
        this.elements = elements;
        this.sourceFileGenerator = sourceFileGenerator;
    }

    @Override
    public Set<String> annotations() {
        return Set.of(Command.class.getCanonicalName());
    }

    @Override
    public Set<? extends Element> process(Map<String, Set<Element>> elementsByAnnotation) {
        List<Element> elements = elementsByAnnotation.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toList());
        ElementFilter.typesIn(elements)
                .forEach(element ->
                        validateSourceElement(element).ifLeftOrElse(
                                this::printFailures,
                                this::processSourceElement));
        return Set.of();
    }

    private void processSourceElement(SourceElement sourceElement) {
        CommandProcessor processor = ValidateComponent.builder()
                .sourceElement(sourceElement)
                .types(types)
                .elements(elements)
                .build()
                .processor();
        processor.generate()
                .map(items -> ContextComponent.builder()
                        .commandRepresentation(items.build(sourceElement))
                        .build())
                .ifLeftOrElse(
                        this::printFailures,
                        component -> writeSpecs(sourceElement, List.of(
                                component.parserClass().define(),
                                component.implClass().define())));
    }

    private void writeSpecs(SourceElement sourceElement, List<TypeSpec> typeSpecs) {
        typeSpecs.forEach(typeSpec -> writeSpec(sourceElement, typeSpec));
    }

    private void writeSpec(SourceElement sourceElement, TypeSpec typeSpec) {
        if (typeSpec.originatingElements.size() != 1) {
            throw new AssertionError();
        }
        String packageName = sourceElement.generatedClass().packageName();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build();
        sourceFileGenerator.write(sourceElement, javaFile);
    }

    private Either<List<ValidationFailure>, SourceElement> validateSourceElement(
            TypeElement element) {
        return util.commonTypeChecks(element)
                .map(List::of)
                .<Either<List<ValidationFailure>, SourceElement>>map(Either::left)
                .orElseGet(() -> right(SourceElement.create(element)));
    }

    private void printFailures(List<ValidationFailure> failures) {
        for (ValidationFailure failure : failures) {
            failure.writeTo(messager);
        }
    }
}
