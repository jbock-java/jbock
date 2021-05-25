package net.jbock.compiler;

import dagger.Component;
import net.jbock.compiler.view.GeneratedClass;
import net.jbock.scope.ContextScope;

@Component(modules = ContextModule.class)
@ContextScope
public interface ContextComponent {

  GeneratedClass generatedClass();

  @Component.Factory
  interface Builder {

    ContextComponent create(
        ContextModule module);
  }
}
