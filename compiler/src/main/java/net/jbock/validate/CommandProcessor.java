package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.ConvertModule;
import net.jbock.convert.DaggerConvertComponent;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;
import static io.jbock.util.Eithers.toValidListAll;

/**
 * This class is responsible for item validation.
 * If validation succeeds, an {@link Items} instance is created.
 */
@ValidateScope
public class CommandProcessor {

    private final ParamsFactory paramsFactory;
    private final MethodsFactory methodsFactory;
    private final ConvertModule convertModule;

    @Inject
    CommandProcessor(
            ParamsFactory paramsFactory,
            MethodsFactory methodsFactory,
            ConvertModule convertModule) {
        this.paramsFactory = paramsFactory;
        this.methodsFactory = methodsFactory;
        this.convertModule = convertModule;
    }

    /**
     * Performs validation and creates an instance of {@link Items},
     * if validation succeeds.
     *
     * @return either a list of validation failures, or an instance of
     *         {@code Items}
     */
    public Either<List<ValidationFailure>, Items> generate() {
        return methodsFactory.findAbstractMethods()
                .flatMap(this::createItems);
    }

    private Either<List<ValidationFailure>, Items> createItems(AbstractMethods methods) {
        return createPositionalParams(methods)
                .flatMap(positionalParameters -> createItems(
                        methods.namedOptions(), positionalParameters));
    }

    private Either<List<ValidationFailure>, Items> createItems(
            List<SourceMethod<?>> options,
            List<Mapped<PositionalParameter>> positionalParameters) {
        return options.stream()
                .map(sourceMethod -> DaggerConvertComponent.builder()
                        .module(convertModule)
                        .build()
                        .namedOptionFactory().createNamedOption(sourceMethod))
                .collect(toValidListAll())
                .filter(this::validateUniqueOptionNames)
                .flatMap(namedOptions -> paramsFactory.create(positionalParameters, namedOptions));
    }

    private Optional<List<ValidationFailure>> validateUniqueOptionNames(List<Mapped<NamedOption>> allOptions) {
        Set<String> allNames = new HashSet<>();
        return allOptions.stream()
                .map(Mapped::item)
                .flatMap(item -> item.names().stream()
                        .filter(name -> !allNames.add(name))
                        .map(name -> "duplicate option name: " + name)
                        .map(item::fail))
                .collect(toOptionalList());
    }

    private Either<List<ValidationFailure>, List<Mapped<PositionalParameter>>> createPositionalParams(
            AbstractMethods methods) {
        return methods.positionalParameters().stream()
                .map(sourceMethod -> DaggerConvertComponent.builder()
                        .module(convertModule)
                        .build()
                        .positionalParameterFactory()
                        .createPositionalParam(sourceMethod))
                .collect(toValidListAll())
                .filter(this::validatePositions)
                .filter(this::checkNoRequiredAfterOptional);
    }

    private Optional<List<ValidationFailure>> validatePositions(List<Mapped<PositionalParameter>> allPositionalParameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < allPositionalParameters.size(); i++) {
            PositionalParameter item = allPositionalParameters.get(i).item();
            if (item.position() != i) {
                failures.add(item.fail("invalid position: expecting " + i));
            }
        }
        return optionalList(failures);
    }

    private Optional<List<ValidationFailure>> checkNoRequiredAfterOptional(List<Mapped<PositionalParameter>> allPositionalParameters) {
        return allPositionalParameters.stream()
                .filter(Mapped::isOptional)
                .findFirst()
                .map(Mapped::item)
                .flatMap(firstOptional -> allPositionalParameters.stream()
                        .filter(Mapped::isRequired)
                        .map(Mapped::item)
                        .filter(item -> item.position() > firstOptional.position())
                        .map(item -> item.fail("position of required parameter '" +
                                item.sourceMethod().method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.sourceMethod().method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }
}
