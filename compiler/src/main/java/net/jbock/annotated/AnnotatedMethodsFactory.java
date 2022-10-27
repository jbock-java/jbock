package net.jbock.annotated;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.instancesOf;

@ValidateScope
public class AnnotatedMethodsFactory {

    private final Comparator<AnnotatedParameter> indexComparator =
            Comparator.comparingInt(AnnotatedParameter::index);

    private final SourceElement sourceElement;
    private final ExecutableElementsFinder executableElementsFinder;

    @Inject
    AnnotatedMethodsFactory(
            SourceElement sourceElement,
            ExecutableElementsFinder executableElementsFinder) {
        this.sourceElement = sourceElement;
        this.executableElementsFinder = executableElementsFinder;
    }

    public Either<List<ValidationFailure>, AnnotatedMethods> createAnnotatedMethods() {
        return executableElementsFinder.findExecutableElements()
                .map(SourceElementWithMethods::new)
                .flatMap(SourceElementWithMethods::validListOfAnnotatedMethods)
                .map(executables -> executables.stream().map(Executable::annotatedMethod).collect(toList()))
                .map(AnnotatedMethodsBuilder::builder)
                .map(builder -> builder.withNamedOptions(builder.annotatedMethods()
                        .flatMap(instancesOf(AnnotatedOption.class))
                        .collect(toList())))
                .map(builder -> builder.withPositionalParameters(builder.annotatedMethods()
                        .flatMap(instancesOf(AnnotatedParameter.class))
                        .sorted(indexComparator)
                        .collect(toList())))
                .map(builder -> builder.withVarargsParameters(builder.annotatedMethods()
                        .flatMap(instancesOf(AnnotatedVarargsParameter.class))
                        .collect(toList())))
                .filter(this::validateAtLeastOneParameterInSuperCommand);
    }

    private Optional<List<ValidationFailure>> validateAtLeastOneParameterInSuperCommand(
            AnnotatedMethods annotatedMethods) {
        if (!sourceElement.isSuperCommand() ||
                !annotatedMethods.positionalParameters().isEmpty()) {
            return Optional.empty();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return Optional.of(List.of(sourceElement.fail(message)));
    }
}
