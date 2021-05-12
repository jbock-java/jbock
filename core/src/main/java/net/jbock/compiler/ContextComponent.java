package net.jbock.compiler;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.compiler.view.GeneratedClass;
import net.jbock.convert.ConvertedParameter;

import java.util.List;

@Component(modules = ContextModule.class)
interface ContextComponent {

  GeneratedClass generatedClass();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder params(List<ConvertedParameter<PositionalParameter>> parameters);

    @BindsInstance
    Builder options(List<ConvertedParameter<NamedOption>> options);

    Builder module(ContextModule module);

    ContextComponent build();
  }
}
