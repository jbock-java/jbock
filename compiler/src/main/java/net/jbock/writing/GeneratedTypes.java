package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.Inject;
import net.jbock.util.ParsingFailed;

import static net.jbock.common.Constants.EITHER;

final class GeneratedTypes extends HasCommandRepresentation {

    @Inject
    GeneratedTypes(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    TypeName parseSuccessType() {
        return sourceElement().typeName();
    }

    ClassName implType() {
        return sourceElement().generatedClass().nestedClass(sourceElement().element().getSimpleName() + "_Impl");
    }

    TypeName parseResultType() {
        return ParameterizedTypeName.get(
                EITHER,
                ClassName.get(ParsingFailed.class),
                parseSuccessType());
    }
}
