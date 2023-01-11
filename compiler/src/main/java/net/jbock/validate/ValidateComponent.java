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

public final class ValidateComponent {

    public static Either<List<ValidationFailure>, CommandRepresentation> generate(
            Util util,
            TypeTool tool,
            SourceElement sourceElement) {
        AbstractMethodsFinder abstractMethodsFinder = new AbstractMethodsFinder(sourceElement);
        ItemsFactory itemsFactory = new ItemsFactory(sourceElement, abstractMethodsFinder);
        AutoMappings autoMappings = new AutoMappings(tool);
        AutoOrEnumMapper autoOrEnumMapper = new AutoOrEnumMapper(autoMappings);
        ConverterValidator converterValidator = new ConverterValidator(tool.types(), tool.elements(),
                (TypeElement converter, TypeMirror outputType, boolean supplier) ->
                        new MappingFactory(converter, outputType, supplier, tool.types()));
        OptionalMatcher optionalMatcher = new OptionalMatcher(tool, tool.elements());
        ListMatcher listMatcher = new ListMatcher(tool.elements(), tool);
        Set<Matcher> matchers = Set.of(optionalMatcher, listMatcher);
        MatchFinder matchFinder = new MatchFinder(matchers, tool.types());
        MappingFinder mappingFinder = new MappingFinder(autoOrEnumMapper, converterValidator, sourceElement, util, matchFinder);
        OptionValidator optionValidator = new OptionValidator(mappingFinder);
        ParameterValidator parameterValidator = new ParameterValidator(mappingFinder);
        VarargsParameterValidator varargsParameterValidator = new VarargsParameterValidator(mappingFinder, sourceElement);
        CommandProcessor commandProcessor = new CommandProcessor(itemsFactory, sourceElement, optionValidator, parameterValidator, varargsParameterValidator);
        return commandProcessor.generate();
    }

    private ValidateComponent() {
    }
}
