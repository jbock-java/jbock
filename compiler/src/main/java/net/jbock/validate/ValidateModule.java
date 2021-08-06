package net.jbock.validate;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;

/**
 * @see ValidateScope
 */
@Module
public class ValidateModule {

    private final SafeTypes types;
    private final SafeElements elements;

    public ValidateModule(SafeTypes types, SafeElements elements) {
        this.types = types;
        this.elements = elements;
    }

    @ValidateScope
    @Provides
    SafeTypes types() {
        return types;
    }

    @ValidateScope
    @Provides
    TypeTool tool() {
        return new TypeTool(elements, types);
    }

    @ValidateScope
    @Provides
    Util util(TypeTool tool) {
        return new Util(types, tool);
    }

    @ValidateScope
    @Provides
    SafeElements elements() {
        return elements;
    }
}
