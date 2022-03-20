package net.jbock.writing;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.FieldSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;

class CommonFields {

    private final ClassName optType;

    @AssistedInject
    CommonFields(
            @Assisted SourceElement sourceElement,
            @Assisted List<Mapping<AnnotatedOption>> namedOptions) {
        optType = namedOptions.isEmpty() ?
                ClassName.get(Void.class) : // javapoet #739
                sourceElement.optionEnumType();
    }

    FieldSpec optionNames() {
        return FieldSpec.builder(
                        mapOf(STRING, optType), "optionNames")
                .addModifiers(PRIVATE, FINAL).build();
    }

    /** Returns the type of the option enum. */
    ClassName optType() {
        return optType;
    }

    @AssistedFactory
    interface Factory {
        CommonFields create(SourceElement sourceElement, List<Mapping<AnnotatedOption>> namedOptions);
    }
}
