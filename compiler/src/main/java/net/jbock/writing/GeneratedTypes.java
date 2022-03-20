package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.ParameterizedTypeName;
import io.jbock.javapoet.TypeName;
import jakarta.inject.Inject;
import net.jbock.processor.SourceElement;
import net.jbock.util.ParsingFailed;
import net.jbock.util.SuperResult;

import java.util.Optional;

import static net.jbock.common.Constants.EITHER;

@WritingScope
class GeneratedTypes {

    private final SourceElement sourceElement;
    private final ClassName generatedClass;

    @Inject
    GeneratedTypes(SourceElement sourceElement) {
        this.sourceElement = sourceElement;
        this.generatedClass = sourceElement.generatedClass();
    }

    TypeName parseSuccessType() {
        return superResultType().orElse(sourceElement.typeName());
    }

    Optional<TypeName> superResultType() {
        if (!sourceElement.isSuperCommand()) {
            return Optional.empty();
        }
        ParameterizedTypeName type = ParameterizedTypeName.get(
                ClassName.get(SuperResult.class),
                sourceElement.typeName());
        return Optional.of(type);
    }

    ClassName implType() {
        return generatedClass.peerClass(sourceElement.element().getSimpleName() + "_Impl");
    }

    TypeName parseResultType() {
        return ParameterizedTypeName.get(
                EITHER,
                ClassName.get(ParsingFailed.class),
                parseSuccessType());
    }
}
