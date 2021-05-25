package net.jbock.validate;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.common.OperationMode;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.compiler.SourceElement;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

@Component(modules = CommandModule.class)
@ValidateScope
public interface CommandComponent {

  CommandProcessor processor();

  SourceFileGenerator sourceFileGenerator();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder sourceElement(SourceElement sourceElement);

    @BindsInstance
    Builder tool(TypeTool tool);

    @BindsInstance
    Builder util(Util util);

    @BindsInstance
    Builder filer(Filer filer);

    @BindsInstance
    Builder messager(Messager messager);

    @BindsInstance
    Builder operationMode(OperationMode operationMode);

    CommandComponent create();
  }
}
