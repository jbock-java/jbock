package net.jbock.context;

import dagger.Module;
import dagger.Provides;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

import static net.jbock.common.Constants.concat;

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
    NamedOptions namedOptions(SourceElement sourceElement) {
        return NamedOptions.create(namedOptions, sourceElement.unixClustering());
    }

    @ContextScope
    @Provides
    List<Mapping<?>> everything() {
        return concat(concat(namedOptions, positionalParams), repeatablePositionalParameters);
    }

    @ContextScope
    @Provides
    CommonFields commonFields(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            NamedOptions namedOptions) {
        return CommonFields.create(
                generatedTypes,
                sourceElement,
                positionalParams,
                namedOptions);
    }
}
