package net.jbock.validate;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.Parameters;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.ConvertModule;
import net.jbock.convert.DaggerConvertComponent;
import net.jbock.convert.Mapped;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceOption;
import net.jbock.source.SourceParameter;
import net.jbock.source.SourceParameters;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.optionalList;
import static io.jbock.util.Eithers.toOptionalList;
import static io.jbock.util.Eithers.toValidListAll;
import static java.lang.Character.isWhitespace;
import static net.jbock.common.Constants.concat;

/**
 * This class is responsible for item validation.
 * If validation succeeds, an {@link Items} instance is created.
 */
@ValidateScope
public class CommandProcessor {

    private final ParamsFactory paramsFactory;
    private final MethodsFactory methodsFactory;
    private final ConvertModule convertModule;
    private final SourceElement sourceElement;

    @Inject
    CommandProcessor(
            ParamsFactory paramsFactory,
            MethodsFactory methodsFactory,
            ConvertModule convertModule,
            SourceElement sourceElement) {
        this.paramsFactory = paramsFactory;
        this.methodsFactory = methodsFactory;
        this.convertModule = convertModule;
        this.sourceElement = sourceElement;
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
                .flatMap(positionalParameters -> createRepeatablePositionalParams(methods)
                        .map(repeatablePositionalParameters -> concat(positionalParameters, repeatablePositionalParameters)))
                .flatMap((List<Mapped<PositionalParameter>> positionalParameters) -> createItems(
                        methods.namedOptions(), positionalParameters));
    }

    private Either<List<ValidationFailure>, Items> createItems(
            List<SourceOption> options,
            List<Mapped<PositionalParameter>> positionalParameters) {
        return options.stream()
                .map(this::checkOptionNames)
                .collect(toValidListAll())
                .filter(this::validateUniqueOptionNames)
                .flatMap(sourceOptions -> sourceOptions.stream()
                        .map(sourceMethod -> DaggerConvertComponent.builder()
                                .module(convertModule)
                                .build()
                                .namedOptionFactory().createNamedOption(sourceMethod))
                        .collect(toValidListAll()))
                .flatMap(namedOptions -> paramsFactory.create(positionalParameters, namedOptions));
    }

    private Either<ValidationFailure, SourceOption> checkOptionNames(SourceOption sourceMethod) {
        if (sourceMethod.annotatedMethod().names().isEmpty()) {
            return left(sourceMethod.fail("define at least one option name"));
        }
        for (String name : sourceMethod.names()) {
            Optional<String> check = checkName(name);
            if (check.isPresent()) {
                return left(sourceMethod.fail(check.orElseThrow()));
            }
        }
        return right(sourceMethod);
    }

    /* Left-Optional
     */
    private Optional<String> checkName(String name) {
        if (Objects.toString(name, "").length() <= 1 || "--".equals(name)) {
            return Optional.of("invalid name: " + name);
        }
        if (!name.startsWith("-")) {
            return Optional.of("the name must start with a dash character: " + name);
        }
        if (name.startsWith("---")) {
            return Optional.of("the name must start with one or two dashes, not three:" + name);
        }
        if (!name.startsWith("--") && name.length() > 2) {
            return Optional.of("single-dash names must be single-character names: " + name);
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isWhitespace(c)) {
                return Optional.of("the name contains whitespace characters: " + name);
            }
            if (c == '=') {
                return Optional.of("the name contains '=': " + name);
            }
        }
        return Optional.empty();
    }

    private Optional<List<ValidationFailure>> validateUniqueOptionNames(List<SourceOption> allOptions) {
        Set<String> allNames = new HashSet<>();
        return allOptions.stream()
                .flatMap(item -> item.names().stream()
                        .filter(name -> !allNames.add(name))
                        .map(name -> "duplicate option name: " + name)
                        .map(item::fail))
                .collect(toOptionalList());
    }

    private Either<List<ValidationFailure>, List<Mapped<PositionalParameter>>> createPositionalParams(
            AbstractMethods methods) {
        return validatePositions(methods.positionalParameters())
                .flatMap(positionalParameters -> positionalParameters.stream()
                        .map(sourceMethod -> DaggerConvertComponent.builder()
                                .module(convertModule)
                                .build()
                                .positionalParameterFactory()
                                .createPositionalParam(sourceMethod))
                        .collect(toValidListAll()))
                .filter(this::checkNoRequiredAfterOptional);
    }

    private Either<List<ValidationFailure>, List<Mapped<PositionalParameter>>> createRepeatablePositionalParams(
            AbstractMethods methods) {
        return validateDuplicateParametersAnnotation(methods.repeatablePositionalParameters())
                .filter(this::validateNoRepeatableParameterInSuperCommand)
                .flatMap(repeatablePositionalParameters -> repeatablePositionalParameters.stream()
                        .map(sourceMethod -> DaggerConvertComponent.builder()
                                .module(convertModule)
                                .build()
                                .positionalParameterFactory()
                                .createRepeatablePositionalParam(sourceMethod))
                        .collect(toValidListAll()))
                .filter(this::checkNoRequiredAfterOptional);
    }

    private Either<List<ValidationFailure>, List<SourceParameter>> validatePositions(List<SourceParameter> allPositionalParameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < allPositionalParameters.size(); i++) {
            SourceParameter item = allPositionalParameters.get(i);
            int index = item.annotatedMethod().index();
            if (index != i) {
                failures.add(item.fail("invalid position: expecting " + i + " but found " + index));
            }
        }
        return optionalList(failures)
                .<Either<List<ValidationFailure>, List<SourceParameter>>>map(Either::left)
                .orElseGet(() -> right(allPositionalParameters));
    }

    private Either<List<ValidationFailure>, List<SourceParameters>> validateDuplicateParametersAnnotation(List<SourceParameters> repeatablePositionalParameters) {
        return repeatablePositionalParameters.stream()
                .skip(1)
                .map(param -> param.fail("duplicate @" + Parameters.class.getSimpleName() + " annotation"))
                .collect(Eithers.toOptionalList())
                .<Either<List<ValidationFailure>, List<SourceParameters>>>map(Either::left)
                .orElseGet(() -> right(repeatablePositionalParameters));
    }

    private Optional<List<ValidationFailure>> validateNoRepeatableParameterInSuperCommand(List<SourceParameters> repeatablePositionalParameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        return repeatablePositionalParameters.stream()
                .map(param -> param.fail("@" + Parameters.class.getSimpleName() +
                        " cannot be used when superCommand=true"))
                .collect(Eithers.toOptionalList());
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
