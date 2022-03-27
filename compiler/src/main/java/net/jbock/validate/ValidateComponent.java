package net.jbock.validate;

import dagger.BindsInstance;
import dagger.Subcomponent;
import net.jbock.convert.match.MatchModule;
import net.jbock.processor.SourceElement;

/* subcomponent of ProcessorComponent */
@ValidateScope
@Subcomponent(modules = MatchModule.class)
public interface ValidateComponent {

    CommandProcessor processor();

    @Subcomponent.Builder
    interface Builder {

        @BindsInstance
        Builder sourceElement(SourceElement sourceElement);

        ValidateComponent build();
    }
}
