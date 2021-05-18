package net.jbock.compiler.command;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.compiler.OperationMode;
import net.jbock.compiler.TypeTool;
import net.jbock.convert.Util;
import net.jbock.qualifier.SourceElement;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;

@Component(modules = CommandModule.class)
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
