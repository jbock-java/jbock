package net.jbock.validate;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.common.SafeElements;
import net.jbock.common.SafeTypes;
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

        @BindsInstance
        Builder types(SafeTypes types);

        @BindsInstance
        Builder elements(SafeElements elements);

        ValidateComponent build();
    }
}
