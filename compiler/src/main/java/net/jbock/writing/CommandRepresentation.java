package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.FieldSpec;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.annotated.VarargsParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ContextBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;
import static net.jbock.common.Suppliers.memoize;

public final class CommandRepresentation {

    private final Supplier<FieldSpec> optionNames = memoize(() -> FieldSpec.builder(
                    mapOf(STRING, optType()), "optionNames")
            .addModifiers(PRIVATE, FINAL).build());

    private final Supplier<ClassName> optType = memoize(() -> namedOptions().isEmpty() ?
            ClassName.get(Void.class) : // javapoet #739
            sourceElement().optionEnumType());

    private final Supplier<List<Mapping<?>>> allMappings = memoize(() -> {
        List<Mapping<?>> result = new ArrayList<>();
        result.addAll(namedOptions());
        result.addAll(positionalParameters());
        varargsParameter().ifPresent(result::add);
        return result;
    });

    private final ContextBuilder contextBuilder;
    private final SourceElement sourceElement;

    public CommandRepresentation(
            ContextBuilder contextBuilder,
            SourceElement sourceElement) {
        this.contextBuilder = contextBuilder;
        this.sourceElement = sourceElement;
    }

    SourceElement sourceElement() {
        return sourceElement;
    }

    Optional<Mapping<VarargsParameter>> varargsParameter() {
        return contextBuilder.varargsParameter();
    }

    List<Mapping<Parameter>> positionalParameters() {
        return contextBuilder.positionalParameters();
    }

    List<Mapping<Option>> namedOptions() {
        return contextBuilder.namedOptions();
    }

    FieldSpec optionNames() {
        return optionNames.get();
    }

    /** Returns the type of the option enum. */
    ClassName optType() {
        return optType.get();
    }

    List<Mapping<?>> allMappings() {
        return allMappings.get();
    }
}
