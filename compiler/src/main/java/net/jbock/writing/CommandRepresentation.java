package net.jbock.writing;

import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.annotated.VarargsParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

public final class CommandRepresentation {

    private final Supplier<List<Mapping<?>>> allMappings = memoize(() -> {
        List<Mapping<?>> result = new ArrayList<>(namedOptions().size() + positionalParameters().size() + 1);
        result.addAll(namedOptions());
        result.addAll(positionalParameters());
        varargsParameter().ifPresent(result::add);
        return result;
    });

    private final List<Mapping<Option>> namedOptions;
    private final List<Mapping<Parameter>> positionalParameters;
    private final Optional<Mapping<VarargsParameter>> varargsParameter;

    private final SourceElement sourceElement;

    public CommandRepresentation(
            SourceElement sourceElement,
            List<Mapping<Option>> namedOptions,
            List<Mapping<Parameter>> positionalParameters,
            Optional<Mapping<VarargsParameter>> varargsParameter) {
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
        this.varargsParameter = varargsParameter;
        this.sourceElement = sourceElement;
    }

    SourceElement sourceElement() {
        return sourceElement;
    }

    Optional<Mapping<VarargsParameter>> varargsParameter() {
        return varargsParameter;
    }

    List<Mapping<Parameter>> positionalParameters() {
        return positionalParameters;
    }

    List<Mapping<Option>> namedOptions() {
        return namedOptions;
    }

    List<Mapping<?>> allMappings() {
        return allMappings.get();
    }
}
