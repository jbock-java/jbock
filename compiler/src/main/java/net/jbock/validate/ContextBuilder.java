package net.jbock.validate;

import net.jbock.annotated.AnnotatedMethods;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;

import java.util.List;

/**
 * A telescoping builder that creates the context module.
 */
public final class ContextBuilder {

    private final Step3 step3;
    private final List<Mapping<AnnotatedOption>> namedOptions;

    private ContextBuilder(Step3 step3, List<Mapping<AnnotatedOption>> namedOptions) {
        this.step3 = step3;
        this.namedOptions = namedOptions;
    }

    static Step1 builder(AnnotatedMethods abstractMethods) {
        return new Step1(abstractMethods);
    }

    static final class Step1 {
        private final AnnotatedMethods abstractMethods;

        private Step1(AnnotatedMethods abstractMethods) {
            this.abstractMethods = abstractMethods;
        }

        Step2 accept(List<Mapping<AnnotatedParameter>> positionalParameters) {
            return new Step2(this, positionalParameters);
        }

        List<AnnotatedParameter> positionalParameters() {
            return abstractMethods.positionalParameters();
        }
    }

    static final class Step2 {
        private final Step1 step1;
        private final List<Mapping<AnnotatedParameter>> positionalParameters;

        private Step2(Step1 step1, List<Mapping<AnnotatedParameter>> positionalParameters) {
            this.step1 = step1;
            this.positionalParameters = positionalParameters;
        }

        List<AnnotatedParameters> repeatablePositionalParameters() {
            return step1.abstractMethods.repeatablePositionalParameters();
        }

        Step3 accept(List<Mapping<AnnotatedParameters>> repeatablePositionalParameters) {
            return new Step3(this, repeatablePositionalParameters);
        }
    }

    static final class Step3 {
        private final Step2 step2;
        private final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters;

        private Step3(Step2 step2, List<Mapping<AnnotatedParameters>> repeatablePositionalParameters) {
            this.step2 = step2;
            this.repeatablePositionalParameters = repeatablePositionalParameters;
        }

        List<AnnotatedOption> namedOptions() {
            return step2.step1.abstractMethods.namedOptions();
        }

        ContextBuilder accept(List<Mapping<AnnotatedOption>> namedOptions) {
            return new ContextBuilder(this, namedOptions);
        }
    }

    public List<Mapping<AnnotatedOption>> namedOptions() {
        return namedOptions;
    }

    public List<Mapping<AnnotatedParameter>> positionalParameters() {
        return step3.step2.positionalParameters;
    }

    public List<Mapping<AnnotatedParameters>> repeatablePositionalParameters() {
        return step3.repeatablePositionalParameters;
    }
}
