package net.jbock.context;

import dagger.Module;
import dagger.Provides;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @see ContextScope
 */
@Module
public class ContextModule {

    private final SourceElement sourceElement;
    private final List<Mapping<AnnotatedParameter>> positionalParams;
    private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;
    private final List<Mapping<AnnotatedOption>> namedOptions;

    public ContextModule(
            SourceElement sourceElement,
            List<Mapping<AnnotatedParameter>> positionalParams,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters,
            List<Mapping<AnnotatedOption>> namedOptions) {
        this.sourceElement = sourceElement;
        this.positionalParams = positionalParams;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.namedOptions = namedOptions;
    }

    @ContextScope
    @Provides
    SourceElement sourceElement() {
        return sourceElement;
    }

    @ContextScope
    @Provides
    List<Mapping<AnnotatedParameters>> repeatablePositionalParameters() {
        return repeatablePositionalParameters;
    }

    @ContextScope
    @Provides
    List<Mapping<AnnotatedParameter>> positionalParameters() {
        return positionalParams;
    }

    @ContextScope
    @Provides
    List<Mapping<AnnotatedOption>> getNamedOptions() {
        return namedOptions;
    }

    @ContextScope
    @Provides
    List<Mapping<?>> allMappings() {
        return Stream.of(namedOptions, positionalParams, repeatablePositionalParameters)
                .flatMap(List::stream)
                .collect(toList());
    }

    @ContextScope
    @Provides
    CommonFields commonFields(
            SourceElement sourceElement) {
        return CommonFields.create(
                sourceElement,
                positionalParams,
                namedOptions);
    }
}
