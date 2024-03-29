package net.jbock.validate;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.Named;
import io.jbock.simple.Provides;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.match.Matcher;
import net.jbock.processor.SourceElement;

import java.util.Set;

@Component
public interface ValidateComponent {

    CommandProcessor commandProcessor();

    @Component.Builder
    interface Builder {
        Builder util(Util util);

        Builder tool(TypeTool tool);

        Builder sourceElement(SourceElement sourceElement);

        ValidateComponent build();
    }

    static ValidateComponent.Builder getBuilder() {
        return ValidateComponent_Impl.builder();
    }

    @Provides
    static Set<Matcher> allMatchers(
            @Named("optional") Matcher optionalMatcher,
            @Named("list") Matcher listMatcher) {
        return Set.of(optionalMatcher, listMatcher);
    }

    class Factory {
        private final Util util;
        private final TypeTool tool;

        @Inject
        public Factory(Util util, TypeTool tool) {
            this.util = util;
            this.tool = tool;
        }

        public ValidateComponent create(SourceElement sourceElement) {
            return ValidateComponent_Impl.builder()
                    .util(util)
                    .tool(tool)
                    .sourceElement(sourceElement)
                    .build();
        }
    }
}
