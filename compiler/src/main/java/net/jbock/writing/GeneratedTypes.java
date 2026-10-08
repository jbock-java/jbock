package net.jbock.writing;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import io.jbock.simple.Inject;
import net.jbock.util.ParsingFailed;

import javax.lang.model.element.Name;

import static net.jbock.common.Constants.EITHER;

@Inject
record GeneratedTypes(CommandRepresentation commandRepresentation) implements HasCommandRepresentation {

    ClassName implType() {
        Name simpleName = sourceElement().element().getSimpleName();
        return sourceElement().generatedClass().nestedClass(simpleName + "_Impl");
    }

    TypeName parseResultType() {
        return ParameterizedTypeName.get(
                EITHER,
                ClassName.get(ParsingFailed.class),
                sourceElement().typeName());
    }
}
