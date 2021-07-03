package net.jbock.validate;

import io.jbock.util.Optional;
import net.jbock.common.Util;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class ParameterMethodValidator {

    private final Util util;

    @Inject
    ParameterMethodValidator(Util util) {
        this.util = util;
    }

    Optional<String> validateParameterMethod(ExecutableElement sourceMethod) {
        List<Class<? extends Annotation>> annotations = methodLevelAnnotations();
        return util.checkAtLeastOneAnnotation(sourceMethod, annotations)
                .or(() -> util.checkNoDuplicateAnnotations(sourceMethod, annotations))
                .or(() -> checkAccessibleType(sourceMethod));
    }

    private Optional<String> checkAccessibleType(ExecutableElement sourceMethod) {
        if (isInaccessible(sourceMethod.getReturnType())) {
            return Optional.of("inaccessible type: " +
                    util.typeToString(sourceMethod.getReturnType()));
        }
        return Optional.empty();
    }

    private boolean isInaccessible(TypeMirror mirror) {
        return AS_DECLARED.visit(mirror)
                .map(declared -> {
                    Element el = declared.asElement();
                    if (el.getModifiers().contains(PRIVATE)) {
                        return true;
                    }
                    boolean badNesting = AS_TYPE_ELEMENT.visit(el)
                            .map(t -> t.getNestingKind() == MEMBER
                                    && !t.getModifiers().contains(STATIC))
                            .orElse(false);
                    return badNesting || declared.getTypeArguments().stream()
                            .anyMatch(this::isInaccessible);
                })
                .orElse(false);
    }
}
