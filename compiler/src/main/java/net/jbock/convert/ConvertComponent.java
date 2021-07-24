package net.jbock.convert;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;

import java.util.List;

@Component(modules = ConvertModule.class)
@ConvertScope
public interface ConvertComponent {

    PositionalParamFactory positionalParameterFactory();

    NamedOptionFactory namedOptionFactory();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder sourceMethod(SourceMethod sourceMethod);

        @BindsInstance
        Builder alreadyCreatedParams(List<Mapped<PositionalParameter>> alreadyCreated);

        Builder module(ConvertModule module);

        ConvertComponent build();
    }
}
