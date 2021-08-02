package net.jbock.processor;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.common.collect.ImmutableSetMultimap;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import io.jbock.util.Either;
import net.jbock.Command;
import net.jbock.common.SafeElements;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.context.ContextComponent;
import net.jbock.context.DaggerContextComponent;
import net.jbock.context.GeneratedClass;
import net.jbock.validate.CommandProcessor;
import net.jbock.validate.DaggerValidateComponent;
import net.jbock.validate.ValidateModule;

import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static io.jbock.util.Either.right;
import static javax.tools.Diagnostic.Kind.ERROR;
import static net.jbock.common.Annotations.typeLevelAnnotations;

/**
 * This step handles the {@link Command} annotation.
 * It performs validation and source generation.
 *
 * @see ProcessorScope
 */
@ProcessorScope
public class CommandProcessingStep implements BasicAnnotationProcessor.Step {

    private final Messager messager;
    private final Util util;
    private final Types types;
    private final SafeElements elements;
    private final SourceFileGenerator sourceFileGenerator;

    @Inject
    CommandProcessingStep(
            Messager messager,
            Util util,
            Types types,
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
    public Set<? extends Element> process(ImmutableSetMultimap<String, Element> elementsByAnnotation) {
        elementsByAnnotation.forEach((annotationName, element) ->
                ElementFilter.typesIn(List.of(element)).stream()
                        .map(this::validateSourceElement)
                        .forEach(either -> either.ifLeftOrElse(
                                this::printFailures,
                                this::processSourceElement)));
        return Set.of();
    }

    private void processSourceElement(SourceElement sourceElement) {
        CommandProcessor processor = DaggerValidateComponent.builder()
                .sourceElement(sourceElement)
                .module(new ValidateModule(types, elements))
                .create()
                .processor();
        processor.generate()
                .map(items -> items.contextModule(sourceElement))
                .map(module -> DaggerContextComponent.factory().create(module))
                .map(ContextComponent::generatedClass)
                .map(GeneratedClass::define)
                .ifLeftOrElse(
                        this::printFailures,
                        typeSpec -> writeSpec(sourceElement, typeSpec));
    }

    private void writeSpec(SourceElement sourceElement, TypeSpec typeSpec) {
        checkArgument(typeSpec.originatingElements.size() == 1);
        String packageName = sourceElement.generatedClass().packageName();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .skipJavaLangImports(true)
                .build();
        sourceFileGenerator.write(sourceElement, javaFile);
    }

    private Either<List<ValidationFailure>, SourceElement> validateSourceElement(
            TypeElement element) {
        return util.commonTypeChecks(element)
                .or(() -> util.checkNoDuplicateAnnotations(element, typeLevelAnnotations()))
                .map(List::of)
                .<Either<List<ValidationFailure>, SourceElement>>map(Either::left)
                .orElseGet(() -> right(SourceElement.create(element)));
    }

    private void printFailures(List<ValidationFailure> failures) {
        for (ValidationFailure failure : failures) {
            messager.printMessage(ERROR, failure.message(), failure.about());
        }
    }
}
