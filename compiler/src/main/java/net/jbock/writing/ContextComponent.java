package net.jbock.writing;

import io.jbock.simple.Component;

@Component
public interface ContextComponent {

    ParserClass parser();

    @Component.Factory
    interface Factory {
        ContextComponent create(CommandRepresentation cr);
    }

    static ContextComponent create(CommandRepresentation cr) {
        return ContextComponent_Impl.factory().create(cr);
    }
}
