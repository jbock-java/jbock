package net.jbock.writing;

import com.palantir.javapoet.ClassName;
import io.jbock.simple.Inject;
import net.jbock.convert.Mapping;
import net.jbock.parse.StandardParser;
import net.jbock.parse.SuperParser;
import net.jbock.parse.VarargsParameterParser;

@Inject
record ParserTypeFactory(CommandRepresentation cr) {

    ClassName get() {
        if (cr.isSuperCommand()) {
            return ClassName.get(SuperParser.class);
        } else if (cr.positionalParameters().stream().anyMatch(Mapping::isRepeatable)) {
            return ClassName.get(VarargsParameterParser.class);
        } else {
            return ClassName.get(StandardParser.class);
        }
    }
}
