package net.jbock.context;

import dagger.Module;
import dagger.Provides;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapped;
import net.jbock.processor.SourceElement;

import java.util.List;

/**
 * @see ContextScope
 */
@Module
public class ContextModule {

    private final SourceElement sourceElement;
    private final List<Mapped<AnnotatedParameter>> positionalParams;
    private final List<Mapped<AnnotatedParameters>> repeatablePositionalParameters;
    private final List<Mapped<AnnotatedOption>> namedOptions;

    public ContextModule(
            SourceElement sourceElement,
            List<Mapped<AnnotatedParameter>> positionalParams,
            List<Mapped<AnnotatedParameters>> repeatablePositionalParameters,
            List<Mapped<AnnotatedOption>> namedOptions) {
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
    PositionalParameters positionalParameters() {
        return PositionalParameters.create(positionalParams, repeatablePositionalParameters);
    }

    @ContextScope
    @Provides
    NamedOptions namedOptions(SourceElement sourceElement) {
        return NamedOptions.create(namedOptions, sourceElement.unixClustering());
    }

    @ContextScope
    @Provides
    AllItems allItems() {
        return AllItems.create(positionalParams, repeatablePositionalParameters, namedOptions);
    }

    @ContextScope
    @Provides
    CommonFields commonFields(
            GeneratedTypes generatedTypes,
            SourceElement sourceElement,
            PositionalParameters positionalParameters,
            NamedOptions namedOptions) {
        return CommonFields.create(
                generatedTypes,
                sourceElement,
                positionalParameters,
                namedOptions);
    }
}
