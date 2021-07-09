package net.jbock.validate;

import com.squareup.javapoet.TypeSpec;
import io.jbock.util.Either;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;
import net.jbock.context.ContextModule;
import net.jbock.context.DaggerContextComponent;
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

    public Either<List<ValidationFailure>, TypeSpec> generate() {
        return methodsFactory.findAbstractMethods()
                .flatMap(this::createPositionalParams)
                .flatMap(this::createNamedOptions)
                .map(this::contextModule)
                .map(module -> DaggerContextComponent.factory().create(module))
                .map(component -> component.generatedClass().define());
    }

    private Either<List<ValidationFailure>, Items> createNamedOptions(
            IntermediateResult intermediateResult) {
        List<ValidationFailure> failures = new ArrayList<>();
        List<Mapped<NamedOption>> namedOptions = new ArrayList<>(intermediateResult.options().size());
        for (SourceMethod sourceMethod : intermediateResult.options()) {
            DaggerConvertComponent.builder()
                    .module(parameterModule())
                    .sourceMethod(sourceMethod)
                    .alreadyCreatedParams(intermediateResult.positionalParameters())
                    .alreadyCreatedOptions(namedOptions)
                    .build()
                    .namedOptionFactory()
                    .createNamedOption()
                    .ifPresentOrElse(failures::add, namedOptions::add);
        }
        if (!failures.isEmpty()) {
            return left(failures);
        }
        return paramsFactory.create(intermediateResult.positionalParameters(), namedOptions);
    }

    private Either<List<ValidationFailure>, IntermediateResult> createPositionalParams(AbstractMethods methods) {
        List<Mapped<PositionalParameter>> positionalParams = new ArrayList<>(methods.positionalParameters().size());
        List<ValidationFailure> failures = new ArrayList<>();
        for (SourceMethod sourceMethod : methods.positionalParameters()) {
            DaggerConvertComponent.builder()
                    .module(parameterModule())
                    .sourceMethod(sourceMethod)
                    .alreadyCreatedParams(positionalParams)
                    .alreadyCreatedOptions(List.of())
                    .build()
                    .positionalParameterFactory()
                    .createPositionalParam(sourceMethod.index().orElse(methods.positionalParameters().size() - 1))
                    .ifPresentOrElse(failures::add, positionalParams::add);
        }
        if (!failures.isEmpty()) {
            return left(failures);
        }
        return IntermediateResult.create(methods.namedOptions(), positionalParams);
    }

    private ConvertModule parameterModule() {
        return new ConvertModule(tool, types, sourceElement, elements);
    }

    private ContextModule contextModule(Items items) {
        return new ContextModule(sourceElement, items.positionalParams(),
                items.namedOptions());
    }
}
