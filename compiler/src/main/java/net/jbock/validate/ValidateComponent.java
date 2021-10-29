package net.jbock.validate;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.processor.SourceElement;

@Component(modules = ValidateModule.class)
@ValidateScope
public interface ValidateComponent {

    CommandProcessor processor();

    static Builder builder() {
        return DaggerValidateComponent.builder();
    }

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder sourceElement(SourceElement sourceElement);

        Builder module(ValidateModule module);

        ValidateComponent create();
    }
}
