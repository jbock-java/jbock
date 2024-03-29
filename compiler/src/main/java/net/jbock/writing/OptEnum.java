package net.jbock.writing;

import io.jbock.javapoet.TypeSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.convert.Mapping;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Defines the inner class *Parser.Opt.
 *
 * @see ParserClass
 */
final class OptEnum extends HasCommandRepresentation {

    @Inject
    OptEnum(CommandRepresentation commandRepresentation) {
        super(commandRepresentation);
    }

    TypeSpec define() {
        TypeSpec.Builder spec = TypeSpec.enumBuilder(sourceElement().optionEnumType());
        for (Mapping<Option> option : namedOptions()) {
            spec.addEnumConstant(option.enumName());
        }
        return spec.addModifiers(PRIVATE)
                .build();
    }
}
