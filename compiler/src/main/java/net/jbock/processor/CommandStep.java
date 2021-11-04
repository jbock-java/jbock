package net.jbock.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.Command;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.context.ContextComponent;
import net.jbock.validate.CommandProcessor;
import net.jbock.validate.ValidateComponent;
import net.jbock.validate.ValidateModule;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.jbock.util.Either.right;

/**
 * This step handles the {@link Command} annotation.
 * It performs validation and source generation.
 *
 * @see ProcessorScope
 */
@ProcessorScope
public class CommandStep implements com.google.auto.common.BasicAnnotationProcessor.Step {

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
        elementsByAnnotation.forEach((annotationName, elements) ->
                ElementFilter.typesIn(elements).stream()
                        .map(this::validateSourceElement)
                        .forEach(either -> either.ifLeftOrElse(
                                this::printFailures,
                                this::processSourceElement)));
        return Set.of();
    }

    private void processSourceElement(SourceElement sourceElement) {
        CommandProcessor processor = ValidateComponent.builder()
                .sourceElement(sourceElement)
                .module(new ValidateModule(types, elements))
                .create()
                .processor();
        processor.generate()
                .map(items -> items.contextModule(sourceElement))
                .map(ContextComponent::create)
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
