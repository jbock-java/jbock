package net.jbock.parameter;

import com.squareup.javapoet.TypeName;
import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.common.EnumName;
import net.jbock.common.SafeElements;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

/**
 * This class represents an {@code abstract} Method in the command class,
 * which can be either an {@link Option} or a {@link Parameter}.
 */
public abstract class AbstractItem {

    private final SourceMethod sourceMethod;
    private final EnumName enumName;

    AbstractItem(
            SourceMethod sourceMethod,
            EnumName enumName) {
        this.sourceMethod = sourceMethod;
        this.enumName = enumName;
    }

    public final List<String> description(SafeElements elements) {
        return sourceMethod.description(elements);
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
        return enumName;
    }

    public abstract String paramLabel();

    public final SourceMethod sourceMethod() {
        return sourceMethod;
    }
}
