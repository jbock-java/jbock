package net.jbock.annotated;

import io.jbock.util.Either;
import io.jbock.util.Eithers;
import net.jbock.common.SnakeName;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.type.DeclaredType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.toValidListAll;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.element.NestingKind.MEMBER;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;

@ValidateScope
public class AnnotatedMethodsFactory {

    private final Comparator<AnnotatedParameter> indexComparator =
            Comparator.comparingInt(AnnotatedParameter::index);

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
                .map(EnumNamesBuilder::create)
                .map(step1 -> step1.withEnumNames(createEnumNames(step1.methods())))
                .flatMap(builder -> builder.methods().stream()
                        .map(sourceMethod -> createAnnotatedMethod(sourceMethod,
                                builder.enumNames().get(sourceMethod.simpleName())))
                        .collect(toValidListAll())
                        .map(ItemSeparationBuilder::create))
                .map(step1 -> step1.withNamedOptions(step1.annotatedMethods()
                        .flatMap(AnnotatedMethod::asAnnotatedOption)
                        .collect(toList())))
                .map(step2 -> step2.withPositionalParameters(step2.annotatedMethods()
                        .flatMap(AnnotatedMethod::asAnnotatedParameter)
                        .sorted(indexComparator)
                        .collect(toList())))
                .map(step3 -> step3.withRepeatablePositionalParameters(step3.annotatedMethods()
                        .flatMap(AnnotatedMethod::asAnnotatedParameters)
                        .collect(toList())))
                .flatMap(this::build);
    }

    private Either<List<ValidationFailure>, AnnotatedMethods> build(ItemSeparationBuilder builder) {
        return Eithers.optionalList(validateAtLeastOneParameterInSuperCommand(builder))
                .<Either<List<ValidationFailure>, AnnotatedMethods>>map(Either::left)
                .orElseGet(() -> right(new AnnotatedMethods(
                        builder.namedOptions(),
                        builder.positionalParameters(),
                        builder.repeatablePositionalParameters())));
    }

    private List<ValidationFailure> validateAtLeastOneParameterInSuperCommand(ItemSeparationBuilder builder) {
        if (!sourceElement.isSuperCommand() ||
                !builder.positionalParameters().isEmpty()) {
            return List.of();
        }
        String message = "at least one positional parameter must be defined" +
                " when the superCommand attribute is set";
        return List.of(sourceElement.fail(message));
    }


    private Map<Name, String> createEnumNames(List<Executable> methods) {
        Set<String> enumNames = new HashSet<>(methods.size());
        Map<Name, String> result = new HashMap<>(methods.size());
        for (Executable method : methods) {
            String simpleName = method.simpleName().toString();
            String enumName = "_".equals(simpleName) ?
                    "_1" : // avoid potential keyword issue
                    SnakeName.create(simpleName).snake('_').toUpperCase(Locale.US);
            while (!enumNames.add(enumName)) {
                String suffix = enumName.endsWith("1") ? "1" : "_1";
                enumName = enumName + suffix;
            }
            result.put(method.simpleName(), enumName);
        }
        return result;
    }

    private Either<ValidationFailure, AnnotatedMethod> createAnnotatedMethod(
            Executable sourceMethod,
            String enumName) {
        ExecutableElement method = sourceMethod.method();
        return util.checkNoDuplicateAnnotations(method, methodLevelAnnotations())
                .<Either<ValidationFailure, AnnotatedMethod>>map(Either::left)
                .orElseGet(() -> right(sourceMethod.annotatedMethod(sourceElement, enumName)))
                .filter(this::checkAccessibleReturnType);
    }

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
