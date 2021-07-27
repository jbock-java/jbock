package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapped;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.optionalList;
import static net.jbock.common.Constants.concat;

@ValidateScope
public class ParamsFactory {

    private final SourceElement sourceElement;

    @Inject
    ParamsFactory(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    Either<List<ValidationFailure>, Items> create(
            List<Mapped<AnnotatedParameter>> positionalParams,
            List<Mapped<AnnotatedParameters>> repeatablePositionalParameter,
            List<Mapped<AnnotatedOption>> namedOptions) {
        return optionalList(checkDuplicateDescriptionKeys(namedOptions, positionalParams, repeatablePositionalParameter))
                .<Either<List<ValidationFailure>, Items>>map(Either::left)
                .orElseGet(() -> right(new Items(positionalParams, repeatablePositionalParameter, namedOptions)));
    }

    // TODO Mapped is not needed, validate this earlier
    private List<ValidationFailure> checkDuplicateDescriptionKeys(
            List<Mapped<AnnotatedOption>> namedOptions,
            List<Mapped<AnnotatedParameter>> positionalParams,
            List<Mapped<AnnotatedParameters>> repeatablePositionalParams) {
        List<ValidationFailure> failures = new ArrayList<>();
        List<Mapped<?>> items =
                concat(concat(namedOptions, positionalParams), repeatablePositionalParams);
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        for (Mapped<?> c : items) {
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
