package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.EnumName;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.toValidListAll;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class AnnotatedMethodsFactory {

    private final Util util;
    private final SourceElement sourceElement;
    private final ExecutableElementsFinder executableElementsFinder;

    @Inject
    AnnotatedMethodsFactory(
            Util util,
            SourceElement sourceElement,
            ExecutableElementsFinder executableElementsFinder) {
        this.util = util;
        this.sourceElement = sourceElement;
        this.executableElementsFinder = executableElementsFinder;
    }

    public Either<List<ValidationFailure>, AnnotatedMethods> createAnnotatedMethods() {
        return executableElementsFinder.findExecutableElements()
                .flatMap(this::createAnnotatedMethods)
                .map(AnnotatedMethodsBuilder.Step3::withNamedOptions)
                .map(AnnotatedMethodsBuilder.Step4::withPositionalParameters)
                .map(AnnotatedMethodsBuilder.Step5::withRepeatablePositionalParameters)
                .flatMap(AnnotatedMethodsBuilder::build);
    }

    private Either<List<ValidationFailure>, AnnotatedMethodsBuilder.Step3> createAnnotatedMethods(
            AnnotatedMethodsBuilder.Step2 step) {
        Map<Name, EnumName> enumNames = step.enumNames();
        return step.methods().stream()
                .map(sourceMethod -> createAnnotatedMethod(sourceMethod,
                        enumNames.get(sourceMethod.simpleName())))
                .collect(toValidListAll())
                .map(step::withAnnotatedMethods);
    }


    private Either<ValidationFailure, AnnotatedMethod> createAnnotatedMethod(
            Executable sourceMethod,
            EnumName enumName) {
        ExecutableElement method = sourceMethod.method();
        return util.checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .<Either<ValidationFailure, AnnotatedMethod>>map(Either::left)
                .orElseGet(() -> right(sourceMethod.annotatedMethod(sourceElement, enumName)))
                .filter(this::checkAccessibleReturnType);
    }

    /* Left-Optional
     */
    private Optional<ValidationFailure> checkAccessibleReturnType(
            AnnotatedMethod annotatedMethod) {
        return AS_DECLARED.visit(annotatedMethod.returnType())
                .filter(this::isInaccessible)
                .map(type -> annotatedMethod.fail("inaccessible type: " +
                        util.typeToString(type)));
    }

    private boolean isInaccessible(DeclaredType declared) {
        if (declared.asElement().getModifiers().contains(PRIVATE)) {
            return true;
        }
        if (AS_TYPE_ELEMENT.visit(declared.asElement())
                .filter(t -> t.getNestingKind() == MEMBER)
                .filter(t -> !t.getModifiers().contains(STATIC))
                .isPresent()) {
            return true;
        }
        return declared.getTypeArguments().stream()
                .map(AS_DECLARED::visit)
                .flatMap(Optional::stream)
                .anyMatch(this::isInaccessible);
    }
}
