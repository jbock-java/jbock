package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.javapoet.TypeName;
import io.jbock.simple.Inject;
import net.jbock.util.ParsingFailed;
import net.jbock.util.SuperResult;

import java.util.Optional;

import static net.jbock.common.Constants.EITHER;

final class GeneratedTypes extends HasCommandRepresentation {

    @Inject
    GeneratedTypes(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    TypeName parseSuccessType() {
        return superResultType().orElse(sourceElement().typeName());
    }

    Optional<TypeName> superResultType() {
        if (!isSuperCommand()) {
            return Optional.empty();
        }
        ParameterizedTypeName type = ParameterizedTypeName.get(
                ClassName.get(SuperResult.class),
                sourceElement().typeName());
        return Optional.of(type);
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
