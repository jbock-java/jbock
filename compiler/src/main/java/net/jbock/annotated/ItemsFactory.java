package net.jbock.annotated;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;

import java.util.List;

@Inject
public record ItemsFactory(
        AbstractMethodsFinder abstractMethodsFinder,
        SourceElement sourceElement) {

    public Either<List<ValidationFailure>, Items> createItems() {
        return abstractMethodsFinder.findAbstractMethods()
                .filterMap(methods -> Items.createItems(sourceElement, methods))
                .filter(Items::validate);
    }
}
