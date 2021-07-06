package net.jbock.context;

import dagger.Module;
import dagger.Provides;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import java.util.List;

@Module
public class ContextModule {

    private final SourceElement sourceElement;
    private final List<Mapped<PositionalParameter>> positionalParams;
    private final List<Mapped<NamedOption>> namedOptions;

    public ContextModule(
            SourceElement sourceElement,
            List<Mapped<PositionalParameter>> positionalParams,
            List<Mapped<NamedOption>> namedOptions) {
        this.sourceElement = sourceElement;
        this.positionalParams = positionalParams;
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
        return PositionalParameters.create(positionalParams);
    }

    @ContextScope
    @Provides
    NamedOptions namedOptions(SourceElement sourceElement) {
        return NamedOptions.create(namedOptions, sourceElement.unixClustering());
    }

    @ContextScope
    @Provides
    AllItems allItems() {
        return AllItems.create(positionalParams, namedOptions);
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
