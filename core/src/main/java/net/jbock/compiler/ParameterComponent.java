package net.jbock.compiler;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.SourceMethod;

import java.util.List;

@Component(modules = ParameterModule.class)
interface ParameterComponent {

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
