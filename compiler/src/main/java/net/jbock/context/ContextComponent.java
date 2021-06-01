package net.jbock.context;

import dagger.Component;

@Component(modules = ContextModule.class)
@ContextScope
public interface ContextComponent {

  GeneratedClass generatedClass();

  AtFileReader atFileReader();

  @Component.Factory
  interface Builder {

    ContextComponent create(ContextModule module);
  }
}
