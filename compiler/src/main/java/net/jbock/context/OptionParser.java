package net.jbock.context;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.util.ExToken;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

/**
 * Generates the inner class OptionParser and its subtypes.
 */
@ContextScope
public final class OptionParser {

    private final GeneratedTypes generatedTypes;
    private final UnixClustering unixClustering;
    private final FlagParser flagParser;
    private final RepeatableOptionParser repeatableOptionParser;
    private final RegularOptionParser regularOptionParser;
    private final ReadOptionArgumentMethod readOptionArgumentMethod;

    private final boolean anyRepeatableOptions;
    private final boolean anyRegularOptions; // any (optional|required) ?
    private final boolean anyModeFlags;

    @Inject
    OptionParser(
            GeneratedTypes generatedTypes,
            UnixClustering unixClustering,
            List<Mapping<AnnotatedOption>> options,
            FlagParser flagParser,
            RepeatableOptionParser repeatableOptionParser,
            RegularOptionParser regularOptionParser,
            ReadOptionArgumentMethod readOptionArgumentMethod) {
        this.anyRepeatableOptions = options.stream().anyMatch(Mapping::isRepeatable);
        this.anyRegularOptions = options.stream().anyMatch(
                option -> option.isOptional() || option.isRequired());
        this.anyModeFlags = options.stream().anyMatch(Mapping::modeFlag);
        this.generatedTypes = generatedTypes;
        this.unixClustering = unixClustering;
        this.flagParser = flagParser;
        this.repeatableOptionParser = repeatableOptionParser;
        this.regularOptionParser = regularOptionParser;
        this.readOptionArgumentMethod = readOptionArgumentMethod;
    }

    List<TypeSpec> define() {
        List<TypeSpec> result = new ArrayList<>();
        result.add(defineAbstractOptionParser());
        if (anyModeFlags) {
            result.add(flagParser.define());
        }
        if (anyRepeatableOptions) {
            result.add(repeatableOptionParser.define());
        }
        if (anyRegularOptions) {
            result.add(regularOptionParser.define());
        }
        return result;
    }

    private TypeSpec defineAbstractOptionParser() {
        TypeSpec.Builder spec = TypeSpec.classBuilder(generatedTypes.optionParserType());
        spec.addMethod(readMethodAbstract());
        spec.addMethod(streamMethodAbstract());
        if (anyRepeatableOptions || anyRegularOptions) {
            spec.addMethod(readOptionArgumentMethod.get());
        }
        spec.addModifiers(PRIVATE, STATIC, ABSTRACT);
        return spec.build();
    }

    private MethodSpec readMethodAbstract() {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
        return MethodSpec.methodBuilder("read")
                .addException(ExToken.class)
                .addParameters(List.of(token, it))
                .addModifiers(ABSTRACT)
                .returns(unixClustering.readMethodReturnType())
                .build();
    }

    MethodSpec streamMethodAbstract() {
        ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
        return MethodSpec.methodBuilder("stream")
                .returns(streamOfString)
                .addModifiers(ABSTRACT)
                .build();
    }
}
