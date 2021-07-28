package net.jbock.convert;

import dagger.Lazy;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.matching.AutoConverterFinder;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.source.SourceMethod;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.util.Types;

import static io.jbock.util.Either.right;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.convert.Mapped.createFlag;

@ValidateScope
public class ConverterFinder {

    private final Lazy<AutoConverterFinder> autoConverterFinder;
    private final Lazy<ConverterValidator> converterValidator;

    @Inject
    ConverterFinder(
            Lazy<AutoConverterFinder> autoConverterFinder,
            Lazy<ConverterValidator> converterValidator) {
        this.autoConverterFinder = autoConverterFinder;
        this.converterValidator = converterValidator;
    }

    public <M extends AnnotatedMethod> Either<String, Mapped<M>> findConverter(SourceMethod<M> parameter) {
        return parameter.annotatedMethod().converter()
                .map(converter -> converterValidator.get().validate(parameter, converter))
                .orElseGet(() -> autoConverterFinder.get().findConverter(parameter));
    }
}