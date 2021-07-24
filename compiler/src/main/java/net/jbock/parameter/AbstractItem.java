package net.jbock.parameter;

import com.squareup.javapoet.TypeName;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

public abstract class AbstractItem {

    private final SourceMethod sourceMethod;

    AbstractItem(SourceMethod sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    public final List<String> description() {
        return sourceMethod.description();
    }

    public final String methodName() {
        return sourceMethod.method().getSimpleName().toString();
    }

    public final TypeName returnType() {
        return TypeName.get(sourceMethod.method().getReturnType());
    }

    public final Optional<String> descriptionKey() {
        return sourceMethod.descriptionKey();
    }

    public final List<Modifier> getAccessModifiers() {
        return sourceMethod.accessModifiers();
    }

    public final ValidationFailure fail(String message) {
        return sourceMethod.fail(message);
    }

    public final EnumName enumName() {
        return sourceMethod.enumName();
    }

    public abstract String paramLabel();

    public final SourceMethod sourceMethod() {
        return sourceMethod;
    }
}
