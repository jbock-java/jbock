package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import dagger.BindsInstance;
import dagger.Component;
import net.jbock.coerce.ConvertedParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.lang.model.element.ExecutableElement;

@Component(modules = ParameterModule.class)
interface ParameterComponent {

  PositionalParamFactory positionalParameterFactory();

  NamedOptionFactory namedOptionFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder sourceMethod(ExecutableElement sourceMethod);

    @BindsInstance
    Builder alreadyCreatedParams(ImmutableList<ConvertedParameter<PositionalParameter>> alreadyCreated);

    @BindsInstance
    Builder alreadyCreatedOptions(ImmutableList<ConvertedParameter<NamedOption>> alreadyCreated);

    Builder module(ParameterModule module);

    ParameterComponent build();
  }
}
