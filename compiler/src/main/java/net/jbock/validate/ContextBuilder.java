package net.jbock.validate;

import net.jbock.annotated.Items;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.annotated.VarargsParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.writing.CommandRepresentation;

import java.util.List;
import java.util.Optional;

/**
 * A telescoping builder that creates the command representation.
 */
public final class ContextBuilder {

    private final Step3 step3;
    private final List<Mapping<Option>> namedOptions;

    private ContextBuilder(Step3 step3, List<Mapping<Option>> namedOptions) {
        this.step3 = step3;
        this.namedOptions = namedOptions;
    }

    static Step0 builder(Items items) {
        return new Step0(items);
    }

    static final class Step0 {
        private final Items items;

        Step0(Items items) {
            this.items = items;
        }

        Step1 accept(SourceElement sourceElement) {
            return new Step1(this, sourceElement);
        }
    }

    static final class Step1 {
        private final Step0 step0;
        private final SourceElement sourceElement;

        private Step1(Step0 step0, SourceElement sourceElement) {
            this.step0 = step0;
            this.sourceElement = sourceElement;
        }

        Step2 accept(List<Mapping<Parameter>> positionalParameters) {
            return new Step2(this, positionalParameters);
        }

        List<Parameter> positionalParameters() {
            return step0.items.positionalParameters();
        }
    }

    static final class Step2 {
        private final Step1 step1;
        private final List<Mapping<Parameter>> positionalParameters;

        private Step2(Step1 step1, List<Mapping<Parameter>> positionalParameters) {
            this.step1 = step1;
            this.positionalParameters = positionalParameters;
        }

        List<VarargsParameter> varargsParameters() {
            return step1.step0.items.varargsParameters();
        }

        Step3 accept(Optional<Mapping<VarargsParameter>> varargsParameter) {
            return new Step3(this, varargsParameter);
        }
    }

    static final class Step3 {
        private final Step2 step2;
        private final Optional<Mapping<VarargsParameter>> varargsParameter;

        private Step3(Step2 step2, Optional<Mapping<VarargsParameter>> varargsParameter) {
            this.step2 = step2;
            this.varargsParameter = varargsParameter;
        }

        List<Option> namedOptions() {
            return step2.step1.step0.items.namedOptions();
        }

        ContextBuilder accept(List<Mapping<Option>> namedOptions) {
            return new ContextBuilder(this, namedOptions);
        }
    }

    public List<Mapping<Option>> namedOptions() {
        return namedOptions;
    }

    public List<Mapping<Parameter>> positionalParameters() {
        return step3.step2.positionalParameters;
    }

    public Optional<Mapping<VarargsParameter>> varargsParameter() {
        return step3.varargsParameter;
    }

    public CommandRepresentation build() {
        return new CommandRepresentation(this, step3.step2.step1.sourceElement);
    }
}
