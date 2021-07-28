package net.jbock.validate;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.Parameters;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.ConverterFinder;
import net.jbock.convert.Mapped;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceMethod;
import net.jbock.source.SourceOption;
import net.jbock.source.SourceParameter;
import net.jbock.source.SourceParameters;

import javax.inject.Inject;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.util.Types;
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
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.common.Constants.concat;
import static net.jbock.convert.Mapped.createFlag;

/**
 * This class is responsible for item validation.
 * If validation succeeds, an {@link Items} instance is created.
 */
@ValidateScope
public class CommandProcessor {

    private final MethodsFactory methodsFactory;
    private final SourceElement sourceElement;
    private final ConverterFinder converterFinder;
    private final Types types;

    @Inject
    CommandProcessor(
            MethodsFactory methodsFactory,
            SourceElement sourceElement,
            ConverterFinder converterFinder,
            Types types) {
        this.methodsFactory = methodsFactory;
        this.sourceElement = sourceElement;
        this.converterFinder = converterFinder;
        this.types = types;
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
                .filter(this::checkDuplicateDescriptionKeys)
                .flatMap(this::createItems);
    }

    private Either<List<ValidationFailure>, Items> createItems(AbstractMethods methods) {
        return createPositionalParams(methods)
                .flatMap(positionalParameters -> createRepeatablePositionalParams(methods)
                        .flatMap(repeatablePositionalParameters -> createItems(
                                methods.namedOptions(),
                                positionalParameters,
                                repeatablePositionalParameters)));
    }

    private Either<List<ValidationFailure>, Items> createItems(
            List<SourceOption> options,
            List<Mapped<AnnotatedParameter>> positionalParameters,
            List<Mapped<AnnotatedParameters>> repeatablePositionalParameters) {
        return options.stream()
                .map(this::checkOptionNames)
                .collect(toValidListAll())
                .filter(this::validateUniqueOptionNames)
                .flatMap(sourceOptions -> sourceOptions.stream()
                        .map(sourceMethod -> checkFlag(sourceMethod)
                                .<Either<ValidationFailure, Mapped<AnnotatedOption>>>map(Either::right)
                                .orElseGet(() -> converterFinder.findConverter(sourceMethod).mapLeft(sourceMethod::fail)))
                        .collect(toValidListAll()))
                .map(namedOptions -> new Items(
                        positionalParameters, repeatablePositionalParameters, namedOptions));
    }

    private Optional<List<ValidationFailure>> checkDuplicateDescriptionKeys(AbstractMethods methods) {
        List<ValidationFailure> failures = new ArrayList<>();
        List<SourceMethod<?>> items =
                concat(concat(methods.namedOptions(), methods.positionalParameters()), methods.repeatablePositionalParameters());
        Set<String> keys = new HashSet<>();
        sourceElement.descriptionKey().ifPresent(keys::add);
        for (SourceMethod<?> m : items) {
            m.descriptionKey().ifPresent(key -> {
                if (!keys.add(key)) {
                    String message = "duplicate description key: " + key;
                    failures.add(m.fail(message));
                }
            });
        }
        return optionalList(failures);
    }


    /* Right-Optional
     */
    private Optional<Mapped<AnnotatedOption>> checkFlag(SourceOption sourceMethod) {
        if (sourceMethod.annotatedMethod().converter().isPresent()) {
            return Optional.empty();
        }
        if (sourceMethod.returnType().getKind() != BOOLEAN) {
            return Optional.empty();
        }
        PrimitiveType primitiveBoolean = types.getPrimitiveType(BOOLEAN);
        return Optional.of(createFlag(sourceMethod, primitiveBoolean));
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

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateUniqueOptionNames(List<SourceOption> allOptions) {
        Set<String> allNames = new HashSet<>();
        return allOptions.stream()
                .flatMap(item -> item.names().stream()
                        .filter(name -> !allNames.add(name))
                        .map(name -> "duplicate option name: " + name)
                        .map(item::fail))
                .collect(toOptionalList());
    }

    private Either<List<ValidationFailure>, List<Mapped<AnnotatedParameter>>> createPositionalParams(
            AbstractMethods methods) {
        return validatePositions(methods.positionalParameters())
                .flatMap(positionalParameters -> positionalParameters.stream()
                        .map(sourceMethod -> converterFinder.findConverter(sourceMethod).mapLeft(sourceMethod::fail))
                        .collect(toValidListAll()))
                .filter(this::checkNoRequiredAfterOptional);
    }

    private Either<List<ValidationFailure>, List<Mapped<AnnotatedParameters>>> createRepeatablePositionalParams(
            AbstractMethods methods) {
        return validateDuplicateParametersAnnotation(methods.repeatablePositionalParameters())
                .filter(this::validateNoRepeatableParameterInSuperCommand)
                .flatMap(repeatablePositionalParameters -> repeatablePositionalParameters.stream()
                        .map(sourceMethod -> converterFinder.findConverter(sourceMethod).mapLeft(sourceMethod::fail))
                        .collect(toValidListAll()));
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

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateNoRepeatableParameterInSuperCommand(List<SourceParameters> repeatablePositionalParameters) {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        return repeatablePositionalParameters.stream()
                .map(param -> param.fail("@" + Parameters.class.getSimpleName() +
                        " cannot be used when superCommand=true"))
                .collect(Eithers.toOptionalList());
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> checkNoRequiredAfterOptional(
            List<Mapped<AnnotatedParameter>> allPositionalParameters) {
        return allPositionalParameters.stream()
                .filter(Mapped::isOptional)
                .findFirst()
                .map(Mapped::item)
                .flatMap(firstOptional -> allPositionalParameters.stream()
                        .filter(Mapped::isRequired)
                        .map(Mapped::item)
                        .filter(item -> item.annotatedMethod().index() > firstOptional.annotatedMethod().index())
                        .map(item -> item.fail("position of required parameter '" +
                                item.annotatedMethod().method().getSimpleName() +
                                "' is greater than position of optional parameter '" +
                                firstOptional.annotatedMethod().method().getSimpleName() + "'"))
                        .collect(toOptionalList()));
    }
}
