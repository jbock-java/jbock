package net.jbock.annotated;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;

import java.util.List;

@Inject
public class ItemsFactory {

    private final AbstractMethodsFinder abstractMethodsFinder;

    public ItemsFactory(
            AbstractMethodsFinder abstractMethodsFinder) {
        this.abstractMethodsFinder = abstractMethodsFinder;
    }

    public Either<List<ValidationFailure>, Items> createItems() {
        return abstractMethodsFinder.findAbstractMethods()
                .flatMap(Items::createItems)
                .filter(Items::validate);
    }
}
