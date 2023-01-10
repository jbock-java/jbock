package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AbstractMethodsFinder;
import net.jbock.annotated.ItemsFactory;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.MappingFinder;
import net.jbock.convert.map.AutoMappings;
import net.jbock.convert.map.AutoOrEnumMapper;
import net.jbock.convert.map.ConverterValidator;
import net.jbock.convert.map.MappingFactory;
import net.jbock.convert.match.ListMatcher;
import net.jbock.convert.match.MatchFinder;
import net.jbock.convert.match.Matcher;
import net.jbock.convert.match.OptionalMatcher;
import net.jbock.processor.SourceElement;
import net.jbock.writing.CommandRepresentation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

public final class ValidateComponent {

    private final Supplier<AbstractMethodsFinder> abstractMethodsFinderProvider;
    private final Supplier<ItemsFactory> itemsFactoryProvider;
    private final Supplier<AutoMappings> autoMappingsProvider;
    private final Supplier<AutoOrEnumMapper> autoOrEnumMapperProvider;
    private final Supplier<MappingFactory.Factory> factoryProvider;
    private final Supplier<ConverterValidator> converterValidatorProvider;
    private final Supplier<OptionalMatcher> optionalMatcherProvider;
    private final Supplier<ListMatcher> listMatcherProvider;
    private final Supplier<Set<Matcher>> matchersProvider;
    private final Supplier<MatchFinder> matchFinderProvider;
    private final Supplier<MappingFinder> mappingFinderProvider;
    private final Supplier<OptionValidator> optionValidatorProvider;
    private final Supplier<ParameterValidator> parameterValidatorProvider;
    private final Supplier<VarargsParameterValidator> varargsParameterValidatorProvider;
    private final Supplier<CommandProcessor> commandProcessorProvider;

    public ValidateComponent(
            Util util,
            TypeTool tool,
            SourceElement sourceElement) {
        this.abstractMethodsFinderProvider = memoize(() -> new AbstractMethodsFinder(sourceElement));
        this.itemsFactoryProvider = memoize(() -> new ItemsFactory(sourceElement, abstractMethodsFinderProvider.get()));
        this.autoMappingsProvider = memoize(() -> new AutoMappings(tool));
        this.autoOrEnumMapperProvider = memoize(() -> new AutoOrEnumMapper(autoMappingsProvider.get()));
        this.factoryProvider = memoize(() -> (TypeElement converter, TypeMirror outputType, boolean supplier) -> new MappingFactory(converter, outputType, supplier, tool.types()));
        this.converterValidatorProvider = memoize(() -> new ConverterValidator(tool.types(), tool.elements(), factoryProvider.get()));
        this.optionalMatcherProvider = memoize(() -> new OptionalMatcher(tool, tool.elements()));
        this.listMatcherProvider = memoize(() -> new ListMatcher(tool.elements(), tool));
        this.matchersProvider = memoize(() -> Set.of(optionalMatcherProvider.get(), listMatcherProvider.get()));
        this.matchFinderProvider = memoize(() -> new MatchFinder(matchersProvider.get(), tool.types()));
        this.mappingFinderProvider = memoize(() -> new MappingFinder(autoOrEnumMapperProvider.get(), converterValidatorProvider.get(), sourceElement, util, matchFinderProvider.get()));
        this.optionValidatorProvider = memoize(() -> new OptionValidator(mappingFinderProvider.get()));
        this.parameterValidatorProvider = memoize(() -> new ParameterValidator(mappingFinderProvider.get()));
        this.varargsParameterValidatorProvider = memoize(() -> new VarargsParameterValidator(mappingFinderProvider.get(), sourceElement));
        this.commandProcessorProvider = memoize(() -> new CommandProcessor(itemsFactoryProvider.get(), sourceElement, optionValidatorProvider.get(), parameterValidatorProvider.get(), varargsParameterValidatorProvider.get()));
    }

    public Either<List<ValidationFailure>, CommandRepresentation> generate() {
        return commandProcessorProvider.get().generate();
    }
}
