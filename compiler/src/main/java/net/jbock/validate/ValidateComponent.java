package net.jbock.validate;

import io.jbock.simple.Component;
import io.jbock.simple.Inject;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.processor.SourceElement;

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
