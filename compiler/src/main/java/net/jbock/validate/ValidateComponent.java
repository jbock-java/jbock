package net.jbock.validate;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import io.jbock.simple.Named;
import io.jbock.simple.Provides;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.match.ListMatcher;
import net.jbock.convert.match.Matcher;
import net.jbock.convert.match.OptionalMatcher;
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

    @Provides
    @Named("list")
    static Matcher listMatcher(TypeTool tool) {
        return new ListMatcher(tool);
    }

    @Provides
    @Named("optional")
    static Matcher optionalMatcher(TypeTool tool) {
        return new OptionalMatcher(tool);
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

    @Inject
    record Factory(Util util, TypeTool tool) {
        public ValidateComponent create(SourceElement sourceElement) {
            return ValidateComponent_Impl.builder()
                    .util(util)
                    .tool(tool)
                    .sourceElement(sourceElement)
                    .build();
        }
    }
}
