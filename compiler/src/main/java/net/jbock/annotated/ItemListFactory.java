package net.jbock.annotated;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.jbock.util.Eithers.optionalList;
import static net.jbock.common.Annotations.methodLevelAnnotations;
import static net.jbock.common.Constants.CONVERTERS;

final class ItemListFactory {

    static Either<List<ValidationFailure>, Items> createItemList(
            List<ExecutableElement> methods) {
        UniqueNameSet uniqueNameSet = new UniqueNameSet();
        uniqueNameSet.reserve("_");
        uniqueNameSet.reserve(CONVERTERS);
        List<ValidationFailure> failures = new ArrayList<>();
        int io = 0, ip = 0;
        List<Option> options = new ArrayList<>(methods.size());
        List<Parameter> parameters = new ArrayList<>(methods.size());
        for (ExecutableElement method : methods) {
            String safeName = uniqueNameSet.getUniqueName(method.getSimpleName().toString());
            Either<ValidationFailure, Annotation> e = getMethodAnnotation(method);
            if (e.isLeft()) {
                failures.add(e.getLeft().orElseThrow());
                continue;
            }
            Annotation a = e.getRight().orElseThrow();
            switch (a) {
                case net.jbock.Option option -> {
                    SimpleOption so = new SimpleOption(method, option, safeName);
                    options.add(Option.createOption(so, io++));
                }
                case net.jbock.Parameter parameter -> {
                    SimpleParameter sp = new SimpleParameter(method, parameter, safeName);
                    parameters.add(Parameter.createParameter(sp, ip++));
                }
                default -> throw new AssertionError("unknown annotation " + a);
            }

        }
        return optionalList(failures)
                .map(Either::<List<ValidationFailure>, Items>left)
                .orElseGet(() -> Either.right(new Items(options, parameters)));
    }

    private static Either<ValidationFailure, Annotation> getMethodAnnotation(
            ExecutableElement method) {
        return methodLevelAnnotations().stream()
                .map(method::getAnnotation)
                .filter(Objects::nonNull)
                .findFirst()
                .<Either<ValidationFailure, Annotation>>map(Either::right)
                .orElseGet(() -> Either.left(new ValidationFailure("@Option or @Parameter annotation expected", method)));
    }

    private ItemListFactory() {
    }
}
