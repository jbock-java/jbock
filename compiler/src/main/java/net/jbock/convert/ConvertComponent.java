package net.jbock.convert;

import dagger.Component;

@Component(modules = ConvertModule.class)
@ConvertScope
public interface ConvertComponent {

    PositionalParamFactory positionalParameterFactory();

    NamedOptionFactory namedOptionFactory();

    @Component.Builder
    interface Builder {

        Builder module(ConvertModule module);

        ConvertComponent build();
    }
}
