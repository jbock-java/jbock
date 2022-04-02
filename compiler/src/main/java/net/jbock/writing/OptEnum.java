package net.jbock.writing;

import io.jbock.javapoet.TypeSpec;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Defines the inner class *Parser.Opt.
 *
 * @see ParserClass
 */
@WritingScope
final class OptEnum extends HasCommandRepresentation {

    @Inject
    OptEnum(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.enumBuilder(sourceElement().optionEnumType());
        for (Mapping<AnnotatedOption> option : namedOptions()) {
            spec.addEnumConstant(option.enumName());
        }
        return spec.addModifiers(PRIVATE)
                .build();
    }
}
