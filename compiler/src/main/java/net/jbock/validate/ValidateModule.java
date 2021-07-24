package net.jbock.validate;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.convert.ConvertModule;
import net.jbock.processor.SourceElement;

import javax.lang.model.util.Types;

/**
 * @see ValidateScope
 */
@Module
public class ValidateModule {

    private final Types types;
    private final SafeElements elements;

    public ValidateModule(Types types, SafeElements elements) {
        this.types = types;
        this.elements = elements;
    }

    @ValidateScope
    @Provides
    Types types() {
        return types;
    }

    @ValidateScope
    @Provides
    SafeElements elements() {
        return elements;
    }

    @ValidateScope
    @Provides
    ConvertModule convertModule(TypeTool tool, SourceElement sourceElement) {
        return new ConvertModule(tool, types, sourceElement, elements);
    }
}
