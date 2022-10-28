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
public class ItemsFactory {

    private final Comparator<Parameter> indexComparator =
            Comparator.comparingInt(Parameter::index);

    private final SourceElement sourceElement;
    private final AbstractMethodsFinder abstractMethodsFinder;

    @Inject
    ItemsFactory(
            SourceElement sourceElement,
            AbstractMethodsFinder abstractMethodsFinder) {
        this.sourceElement = sourceElement;
        this.abstractMethodsFinder = abstractMethodsFinder;
    }

    public Either<List<ValidationFailure>, Items> createItems() {
        return abstractMethodsFinder.findAbstractMethods()
                .flatMap(ItemListFactory::createItemList)
                .flatMap(ItemListValidator::validate)
                .map(ItemsBuilder::builder)
                .map(builder -> builder.withNamedOptions(builder.annotatedMethods()
                        .flatMap(instancesOf(Option.class))
                        .collect(toList())))
                .map(builder -> builder.withPositionalParameters(builder.annotatedMethods()
                        .flatMap(instancesOf(Parameter.class))
                        .sorted(indexComparator)
                        .collect(toList())))
                .map(builder -> builder.withVarargsParameters(builder.annotatedMethods()
                        .flatMap(instancesOf(VarargsParameter.class))
                        .collect(toList())))
                .filter(this::validateAtLeastOneParameterInSuperCommand);
    }

    private Optional<List<ValidationFailure>> validateAtLeastOneParameterInSuperCommand(
            Items items) {
        if (!sourceElement.isSuperCommand() ||
                !items.positionalParameters().isEmpty()) {
            return Optional.empty();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return Optional.of(List.of(sourceElement.fail(message)));
    }
}
