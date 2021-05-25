package net.jbock.compiler;

import dagger.Component;
import net.jbock.compiler.view.GeneratedClass;
import net.jbock.scope.AssembleScope;

@Component(modules = ContextModule.class)
@AssembleScope
public interface ContextComponent {

  GeneratedClass generatedClass();

  @Component.Factory
  interface Builder {

    ContextComponent create(
        ContextModule module);
  }
}
