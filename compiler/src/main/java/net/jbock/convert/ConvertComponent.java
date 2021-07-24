package net.jbock.convert;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.parameter.SourceMethod;

@Component(modules = ConvertModule.class)
@ConvertScope
public interface ConvertComponent {

    PositionalParamFactory positionalParameterFactory();

    NamedOptionFactory namedOptionFactory();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder sourceMethod(SourceMethod sourceMethod);

        Builder module(ConvertModule module);

        ConvertComponent build();
    }
}
