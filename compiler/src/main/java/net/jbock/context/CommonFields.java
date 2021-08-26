package net.jbock.context;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;

public class CommonFields {

    private final FieldSpec optionNames;
    private final ClassName optType;

    private CommonFields(
            FieldSpec optionNames,
            ClassName optType) {
        this.optionNames = optionNames;
        this.optType = optType;
    }

    static CommonFields create(
            SourceElement sourceElement,
            List<Mapping<AnnotatedOption>> namedOptions) {
        ClassName optType = namedOptions.isEmpty() ?
                ClassName.get(Void.class) :
                sourceElement.optionEnumType();
        FieldSpec optionNames = FieldSpec.builder(
                        mapOf(STRING, optType), "optionNames")
                .addModifiers(PRIVATE, FINAL).build();
        return new CommonFields(optionNames, optType);
    }

    FieldSpec optionNames() {
        return optionNames;
    }

    ClassName optType() {
        return optType;
    }
}
