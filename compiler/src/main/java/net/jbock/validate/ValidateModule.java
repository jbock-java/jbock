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
interface ValidateModule {

    @ValidateScope
    @Provides
    static TypeTool tool(SafeTypes types, SafeElements elements) {
        return new TypeTool(elements, types);
    }

    @ValidateScope
    @Provides
    static Util util(SafeTypes types, TypeTool tool) {
        return new Util(types, tool);
    }
}
