package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.util.ExToken;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.STRING_ITERATOR;

@ContextScope
public class RepeatableOptionParser {

    private final GeneratedTypes generatedTypes;
    private final UnixClustering unixClustering;
    private final CommonFields commonFields;
    private final ReadOptionArgumentMethod readOptionArgumentMethod;

    @Inject
    RepeatableOptionParser(
            GeneratedTypes generatedTypes,
            UnixClustering unixClustering,
            CommonFields commonFields,
            ReadOptionArgumentMethod readOptionArgumentMethod) {
        this.generatedTypes = generatedTypes;
        this.unixClustering = unixClustering;
        this.commonFields = commonFields;
        this.readOptionArgumentMethod = readOptionArgumentMethod;
    }

    TypeSpec define() {
        return TypeSpec.classBuilder(generatedTypes.repeatableOptionParserType())
                .superclass(generatedTypes.optionParserType())
                .addField(commonFields.values())
                .addMethod(readMethodRepeatable(commonFields.values()))
                .addMethod(streamMethodRepeatable(commonFields.values()))
                .addModifiers(PRIVATE, STATIC).build();
    }

    private MethodSpec readMethodRepeatable(FieldSpec values) {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec it = ParameterSpec.builder(STRING_ITERATOR, "it").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("if ($N == null) $N = new $T<>()", values, values, ArrayList.class);
        code.addStatement("values.add($N($N, $N))", readOptionArgumentMethod.get(), token, it);
        if (unixClustering.unixClusteringSupported()) {
            code.addStatement("return null");
        }
        return MethodSpec.methodBuilder("read")
                .addException(ExToken.class)
                .addParameters(List.of(token, it))
                .addCode(code.build())
                .returns(unixClustering.readMethodReturnType())
                .build();
    }

    private MethodSpec streamMethodRepeatable(FieldSpec values) {
        ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
        return MethodSpec.methodBuilder("stream")
                .returns(streamOfString)
                .addStatement("return $N == null ? $T.empty() : $N.stream()", values, Stream.class, values)
                .build();
    }
}
