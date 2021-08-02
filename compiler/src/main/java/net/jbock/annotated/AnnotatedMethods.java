package net.jbock.annotated;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class AnnotatedMethods {

    private static final Comparator<AnnotatedParameter> INDEX_COMPARATOR =
            Comparator.comparingInt(AnnotatedParameter::index);

    private final List<AnnotatedOption> namedOptions;
    private final List<AnnotatedParameter> positionalParameters;
    private final List<AnnotatedParameters> repeatablePositionalParameters;

    private AnnotatedMethods(
            List<AnnotatedOption> namedOptions,
            List<AnnotatedParameter> positionalParameters,
            List<AnnotatedParameters> repeatablePositionalParameters) {
        this.positionalParameters = positionalParameters;
        this.repeatablePositionalParameters = repeatablePositionalParameters;
        this.namedOptions = namedOptions;
    }

    static AnnotatedMethods create(List<AnnotatedMethod> methods) {
        List<AnnotatedParameter> params = methods.stream()
                .map(AnnotatedMethod::asAnnotatedParameter)
                .flatMap(Optional::stream)
                .sorted(INDEX_COMPARATOR)
                .collect(Collectors.toList());
        List<AnnotatedParameters> repeatableParams = methods.stream()
                .map(AnnotatedMethod::asAnnotatedParameters)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        List<AnnotatedOption> options = methods.stream()
                .map(AnnotatedMethod::asAnnotatedOption)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        return new AnnotatedMethods(options, params, repeatableParams);
    }

    public List<AnnotatedOption> namedOptions() {
        return namedOptions;
    }

    public List<AnnotatedParameter> positionalParameters() {
        return positionalParameters;
    }

    public List<AnnotatedParameters> repeatablePositionalParameters() {
        return repeatablePositionalParameters;
    }
}
