package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.common.Constants;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapped;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.optionalList;

@ValidateScope
public class ParamsFactory {

    private final SourceElement sourceElement;

    @Inject
    ParamsFactory(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, Items> create(
            List<Mapped<PositionalParameter>> positionalParams,
            List<Mapped<PositionalParameter>> repeatablePositionalParameter,
            List<Mapped<NamedOption>> namedOptions) {
        return optionalList(checkDuplicateDescriptionKeys(namedOptions, positionalParams))
                .<Either<List<ValidationFailure>, Items>>map(Either::left)
                .orElseGet(() -> right(new Items(positionalParams, repeatablePositionalParameter, namedOptions)));
    }

    // TODO Mapped is not needed, validate this earlier
    private List<ValidationFailure> checkDuplicateDescriptionKeys(
            List<Mapped<NamedOption>> namedOptions,
            List<Mapped<PositionalParameter>> positionalParams) {
        List<ValidationFailure> failures = new ArrayList<>();
        List<Mapped<? extends AbstractItem>> items =
                Constants.concat(namedOptions, positionalParams);
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        for (Mapped<? extends AbstractItem> c : items) {
            c.item().descriptionKey().ifPresent(key -> {
                if (!keys.add(key)) {
                    String message = "duplicate description key: " + key;
                    failures.add(c.item().fail(message));
                }
            });
        }
        return failures;
    }
}
