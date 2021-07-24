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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                        methods.namedOptions(), positionalParameters))
                .filter(Items::validatePositions);
    }

    private Either<List<ValidationFailure>, Items> createItems(
            List<SourceMethod> options,
            List<Mapped<PositionalParameter>> positionalParameters) {
        return options.stream()
                .map(sourceMethod -> DaggerConvertComponent.builder()
                        .module(convertModule)
                        .sourceMethod(sourceMethod)
                        .build()
                        .namedOptionFactory()
                        .createNamedOption())
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
                        .sourceMethod(sourceMethod)
                        .build()
                        .positionalParameterFactory()
                        .createPositionalParam(sourceMethod.index().orElse(methods.positionalParameters().size() - 1)))
                .collect(toValidListAll())
                .filter(this::validateUniquePositions)
                .filter(this::checkNoRequiredAfterOptional);
    }

    private Optional<List<ValidationFailure>> validateUniquePositions(List<Mapped<PositionalParameter>> allPositionalParameters) {
        Set<Integer> allNames = new HashSet<>();
        return allPositionalParameters.stream()
                .map(Mapped::item)
                .filter(item -> !allNames.add(item.position()))
                .map(item -> item.fail("duplicate position: " + item.position()))
                .collect(toOptionalList());
    }

    private Optional<List<ValidationFailure>> checkNoRequiredAfterOptional(List<Mapped<PositionalParameter>> allPositionalParameters) {
        return allPositionalParameters.stream()
                .filter(Mapped::isOptional)
                .findFirst()
                .flatMap(firstOptional -> allPositionalParameters.stream()
                        .filter(c -> c.item().position() > firstOptional.item().position())
                        .filter(Mapped::isRequired)
                        .map(c -> c.item().fail("position of required parameter '" +
                                c.item().sourceMethod().method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.item().sourceMethod().method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }
}
