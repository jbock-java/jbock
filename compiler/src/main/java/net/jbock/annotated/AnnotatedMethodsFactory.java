package net.jbock.annotated;

import io.jbock.util.Either;
import javax.inject.Inject;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.Name;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
                .map(EnumNamesBuilder::builder)
                .map(builder -> builder.withSourceElement(sourceElement))
                .map(builder -> builder.withEnumNames(createEnumNames(builder.methods())))
                .flatMap(EnumNames::createAnnotatedMethods)
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

    private Map<Name, String> createEnumNames(List<Executable> methods) {
        Set<String> enumNames = new HashSet<>(methods.size());
        Map<Name, String> result = new HashMap<>(methods.size());
        for (Executable method : methods) {
            String enumName = "_".contentEquals(method.simpleName()) ?
                    "_1" : // avoid potential keyword issue
                    SnakeName.create(method.simpleName()).snake('_').toUpperCase(Locale.US);
            while (!enumNames.add(enumName)) {
                String suffix = enumName.endsWith("1") ? "1" : "_1";
                enumName = enumName + suffix;
            }
            result.put(method.simpleName(), enumName);
        }
        return result;
    }
}
