package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.ConvertModule;
import net.jbock.convert.DaggerConvertComponent;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

/**
 * This class is responsible for item validation.
 * If validation succeeds, an {@link Items} instance is created.
 */
@ValidateScope
public class CommandProcessor {

    private final SourceElement sourceElement;
    private final SafeElements elements;
    private final TypeTool tool;
    private final Types types;
    private final ParamsFactory paramsFactory;
    private final MethodsFactory methodsFactory;

    @Inject
    CommandProcessor(
            SourceElement sourceElement,
            SafeElements elements,
            TypeTool tool,
            Types types,
            ParamsFactory paramsFactory,
            MethodsFactory methodsFactory) {
        this.sourceElement = sourceElement;
        this.elements = elements;
        this.tool = tool;
        this.types = types;
        this.paramsFactory = paramsFactory;
        this.methodsFactory = methodsFactory;
    }

    /**
     * Performs validation and creates an instance of {@code GeneratedClass},
     * if validation succeeds.
     *
     * @return either a list of validation failures, or an instance of
     *         {@code GeneratedClass}
     */
    public Either<List<ValidationFailure>, Items> generate() {
        return methodsFactory.findAbstractMethods()
                .flatMap(this::createItems);
    }

    private Either<List<ValidationFailure>, Items> createItems(AbstractMethods methods) {
        return createPositionalParams(methods)
                .flatMap(positionalParameters -> createItems(
                        methods.namedOptions(), positionalParameters))
                .filter(Items::validatePositions); //filterList ?
    }

    private Either<List<ValidationFailure>, Items> createItems(
            List<SourceMethod> options,
            List<Mapped<PositionalParameter>> positionalParameters) {
        List<ValidationFailure> failures = new ArrayList<>();
        List<Mapped<NamedOption>> namedOptions = new ArrayList<>(options.size());
        ConvertModule convertModule = convertModule();
        for (SourceMethod sourceMethod : options) {
            DaggerConvertComponent.builder()
                    .module(convertModule)
                    .sourceMethod(sourceMethod)
                    .alreadyCreatedParams(positionalParameters)
                    .alreadyCreatedOptions(namedOptions)
                    .build()
                    .namedOptionFactory()
                    .createNamedOption()
                    .ifLeftOrElse(failures::add, namedOptions::add);
        }
        if (!failures.isEmpty()) {
            return left(failures);
        }
        return paramsFactory.create(positionalParameters, namedOptions);
    }

    private Either<List<ValidationFailure>, List<Mapped<PositionalParameter>>> createPositionalParams(
            AbstractMethods methods) {
        List<Mapped<PositionalParameter>> positionalParams = new ArrayList<>(methods.positionalParameters().size());
        List<ValidationFailure> failures = new ArrayList<>();
        ConvertModule convertModule = convertModule();
        for (SourceMethod sourceMethod : methods.positionalParameters()) {
            DaggerConvertComponent.builder()
                    .module(convertModule)
                    .sourceMethod(sourceMethod)
                    .alreadyCreatedParams(positionalParams)
                    .alreadyCreatedOptions(List.of())
                    .build()
                    .positionalParameterFactory()
                    .createPositionalParam(sourceMethod.index().orElse(methods.positionalParameters().size() - 1))
                    .ifLeftOrElse(failures::add, positionalParams::add);
        }
        if (!failures.isEmpty()) {
            return left(failures);
        }
        return right(positionalParams);
    }

    private ConvertModule convertModule() {
        return new ConvertModule(tool, types, sourceElement, elements);
    }
}
