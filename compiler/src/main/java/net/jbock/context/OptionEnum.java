package net.jbock.context;

import com.squareup.javapoet.TypeSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.EnumName;
import net.jbock.convert.Mapped;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;

import static javax.lang.model.element.Modifier.PRIVATE;

/**
 * Defines the inner class *Parser.Option.
 *
 * @see GeneratedClass
 */
@ContextScope
public class OptionEnum {

    private final NamedOptions options;
    private final SourceElement sourceElement;

    @Inject
    OptionEnum(
            NamedOptions options,
            SourceElement sourceElement) {
        this.options = options;
        this.sourceElement = sourceElement;
    }

    TypeSpec define() {
        List<Mapped<AnnotatedOption>> parameters = options.options();
        TypeSpec.Builder spec = TypeSpec.enumBuilder(sourceElement.optionEnumType());
        for (Mapped<AnnotatedOption> param : parameters) {
            EnumName enumName = param.enumName();
            String enumConstant = enumName.enumConstant();
            spec.addEnumConstant(enumConstant);
        }
        return spec.addModifiers(PRIVATE)
                .build();
    }
}
