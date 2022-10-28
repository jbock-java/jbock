package net.jbock.annotated;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import java.util.List;
import java.util.Optional;

@ValidateScope
public class ItemsFactory {

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
                .map(Items::createItems)
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
