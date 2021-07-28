package net.jbock.validate;

import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.context.ContextModule;
import net.jbock.convert.Mapped;
import net.jbock.processor.SourceElement;
import net.jbock.source.SourceOption;
import net.jbock.source.SourceParameter;
import net.jbock.source.SourceParameters;

import java.util.List;

/**
 * A telescoping builder that creates the context module.
 */
public final class ContextBuilder {

    private final Step3 step3;
    private final List<Mapped<AnnotatedOption>> namedOptions;

    private ContextBuilder(Step3 step3, List<Mapped<AnnotatedOption>> namedOptions) {
        this.step3 = step3;
        this.namedOptions = namedOptions;
    }

    static Step1 builder(AbstractMethods abstractMethods) {
        return new Step1(abstractMethods);
    }

    static final class Step1 {
        private final AbstractMethods abstractMethods;

        private Step1(AbstractMethods abstractMethods) {
            this.abstractMethods = abstractMethods;
        }

        Step2 accept(List<Mapped<AnnotatedParameter>> positionalParameters) {
            return new Step2(this, positionalParameters);
        }

        List<SourceParameter> positionalParameters() {
            return abstractMethods.positionalParameters();
        }
    }

    static final class Step2 {
        private final Step1 step1;
        private final List<Mapped<AnnotatedParameter>> positionalParameters;

        private Step2(Step1 step1, List<Mapped<AnnotatedParameter>> positionalParameters) {
            this.step1 = step1;
            this.positionalParameters = positionalParameters;
        }

        List<SourceParameters> repeatablePositionalParameters() {
            return step1.abstractMethods.repeatablePositionalParameters();
        }

        Step3 accept(List<Mapped<AnnotatedParameters>> repeatablePositionalParameters) {
            return new Step3(this, repeatablePositionalParameters);
        }
    }

    static final class Step3 {
        private final Step2 step2;
        private final List<Mapped<AnnotatedParameters>> repeatablePositionalParameters;

        private Step3(Step2 step2, List<Mapped<AnnotatedParameters>> repeatablePositionalParameters) {
            this.step2 = step2;
            this.repeatablePositionalParameters = repeatablePositionalParameters;
        }

        List<SourceOption> namedOptions() {
            return step2.step1.abstractMethods.namedOptions();
        }

        ContextBuilder accept(List<Mapped<AnnotatedOption>> namedOptions) {
            return new ContextBuilder(this, namedOptions);
        }
    }

    /**
     * Creates the context module.
     *
     * @param sourceElement the command class
     * @return the context module
     */
    public ContextModule contextModule(SourceElement sourceElement) {
        return new ContextModule(sourceElement,
                step3.step2.positionalParameters,
                step3.repeatablePositionalParameters,
                namedOptions);
    }
}
