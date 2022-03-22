package net.jbock.annotated;

import net.jbock.processor.SourceElement;

import javax.lang.model.element.Name;
import java.util.List;
import java.util.Map;

final class EnumNamesBuilder {

    static Step1 builder(List<Executable> methods) {
        return new Step1(methods);
    }

    static final class Step1 {

        private final List<Executable> methods;

        private Step1(List<Executable> methods) {
            this.methods = methods;
        }

        Step2 withSourceElement(SourceElement sourceElement) {
            return new Step2(this, sourceElement);
        }
    }

    static final class Step2 {

        final Step1 step1;
        final SourceElement sourceElement;

        private Step2(Step1 step1, SourceElement sourceElement) {
            this.step1 = step1;
            this.sourceElement = sourceElement;
        }

        List<Executable> methods() {
            return step1.methods;
        }

        EnumNames withEnumNames(Map<Name, String> enumNames) {
            return new EnumNames(this, enumNames);
        }
    }

    private EnumNamesBuilder() {
    }
}
