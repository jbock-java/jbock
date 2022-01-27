package net.jbock.context;

import io.jbock.javapoet.TypeSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Defines the inner class *Parser.Opt.
 *
 * @see ParserClass
 */
@ContextScope
public class OptEnum {

    private final List<Mapping<AnnotatedOption>> options;
    private final SourceElement sourceElement;

    @Inject
    OptEnum(List<Mapping<AnnotatedOption>> options,
            SourceElement sourceElement) {
        this.options = options;
        this.sourceElement = sourceElement;
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.enumBuilder(sourceElement.optionEnumType());
        for (Mapping<AnnotatedOption> option : options) {
            spec.addEnumConstant(option.enumName());
        }
        return spec.addModifiers(PRIVATE)
                .build();
    }
}
