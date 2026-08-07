package net.jbock.writing;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import io.jbock.simple.Inject;
import net.jbock.util.ParsingFailed;

import static net.jbock.common.Constants.EITHER;

final class GeneratedTypes extends HasCommandRepresentation {

    @Inject
    GeneratedTypes(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    ClassName implType() {
        return sourceElement().generatedClass().nestedClass(sourceElement().element().getSimpleName() + "_Impl");
    }

    TypeName parseResultType() {
        return ParameterizedTypeName.get(
                EITHER,
                ClassName.get(ParsingFailed.class),
                sourceElement().typeName());
    }
}
