package net.jbock.validate;

import net.jbock.annotated.AnnotatedMethods;
import net.jbock.annotated.ExecutableOption;
import net.jbock.annotated.ExecutableParameter;
import net.jbock.annotated.ExecutableVarargsParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;
import net.jbock.writing.CommandRepresentation;

import java.util.List;

/**
 * A telescoping builder that creates the command representation.
 */
public final class ContextBuilder {

    private final Step3 step3;
    private final List<Mapping<ExecutableOption>> namedOptions;

    private ContextBuilder(Step3 step3, List<Mapping<ExecutableOption>> namedOptions) {
        this.step3 = step3;
        this.namedOptions = namedOptions;
    }

    static Step0 builder(AnnotatedMethods abstractMethods) {
        return new Step0(abstractMethods);
    }

    static final class Step0 {
        private final AnnotatedMethods abstractMethods;

        Step0(AnnotatedMethods abstractMethods) {
            this.abstractMethods = abstractMethods;
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

        Step2 accept(List<Mapping<ExecutableParameter>> positionalParameters) {
            return new Step2(this, positionalParameters);
        }

        List<ExecutableParameter> positionalParameters() {
            return step0.abstractMethods.positionalParameters();
        }
    }

    static final class Step2 {
        private final Step1 step1;
        private final List<Mapping<ExecutableParameter>> positionalParameters;

        private Step2(Step1 step1, List<Mapping<ExecutableParameter>> positionalParameters) {
            this.step1 = step1;
            this.positionalParameters = positionalParameters;
        }

        List<ExecutableVarargsParameter> varargsParameters() {
            return step1.step0.abstractMethods.varargsParameters();
        }

        Step3 accept(List<Mapping<ExecutableVarargsParameter>> varargsParameters) {
            return new Step3(this, varargsParameters);
        }
    }

    static final class Step3 {
        private final Step2 step2;
        private final List<Mapping<ExecutableVarargsParameter>> varargsParameters;

        private Step3(Step2 step2, List<Mapping<ExecutableVarargsParameter>> varargsParameters) {
            this.step2 = step2;
            this.varargsParameters = varargsParameters;
        }

        List<ExecutableOption> namedOptions() {
            return step2.step1.step0.abstractMethods.namedOptions();
        }

        ContextBuilder accept(List<Mapping<ExecutableOption>> namedOptions) {
            return new ContextBuilder(this, namedOptions);
        }
    }

    public List<Mapping<ExecutableOption>> namedOptions() {
        return namedOptions;
    }

    public List<Mapping<ExecutableParameter>> positionalParameters() {
        return step3.step2.positionalParameters;
    }

    public List<Mapping<ExecutableVarargsParameter>> varargsParameters() {
        return step3.varargsParameters;
    }

    public CommandRepresentation build() {
        return new CommandRepresentation(this, step3.step2.step1.sourceElement);
    }
}
