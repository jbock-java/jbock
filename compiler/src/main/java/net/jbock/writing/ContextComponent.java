package net.jbock.writing;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Component;

@Component(omitMockBuilder = true)
public interface ContextComponent {

    ParserClass parser();

    @Component.Factory
    interface Factory {
        ContextComponent create(CommandRepresentation command);
    }

    static TypeSpec parserClass(CommandRepresentation command) {
        ContextComponent component = ContextComponent_Impl.factory().create(command);
        return component.parser().define();
    }
}
