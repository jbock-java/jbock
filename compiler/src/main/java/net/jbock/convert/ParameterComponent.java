package net.jbock.convert;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.validate.SourceMethod;

import java.util.List;

@Component(modules = ParameterModule.class)
@ParameterScope
public interface ParameterComponent {

  PositionalParamFactory positionalParameterFactory();

  NamedOptionFactory namedOptionFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder sourceMethod(SourceMethod sourceMethod);

    @BindsInstance
    Builder alreadyCreatedParams(List<ConvertedParameter<PositionalParameter>> alreadyCreated);

    @BindsInstance
    Builder alreadyCreatedOptions(List<ConvertedParameter<NamedOption>> alreadyCreated);

    Builder module(ParameterModule module);

    ParameterComponent build();
  }
}
