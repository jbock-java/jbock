package net.jbock.context;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import net.jbock.common.Constants;
import net.jbock.util.ExToken;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Stream;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static net.jbock.common.Constants.OPTIONAL;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class RegularOptionParser {

    private final GeneratedTypes generatedTypes;
    private final UnixClustering unixClustering;
    private final CommonFields commonFields;
    private final ReadOptionArgumentMethod readOptionArgumentMethod;
    private final ContextUtil contextUtil;

    @Inject
    RegularOptionParser(
            GeneratedTypes generatedTypes,
            UnixClustering unixClustering,
            CommonFields commonFields,
            ReadOptionArgumentMethod readOptionArgumentMethod,
            ContextUtil contextUtil) {
        this.generatedTypes = generatedTypes;
        this.unixClustering = unixClustering;
        this.commonFields = commonFields;
        this.readOptionArgumentMethod = readOptionArgumentMethod;
        this.contextUtil = contextUtil;
    }

    TypeSpec define() {
        return TypeSpec.classBuilder(generatedTypes.regularOptionParserType())
                .superclass(generatedTypes.optionParserType())
                .addField(commonFields.value())
                .addMethod(readMethodRegular(commonFields.value()))
                .addMethod(streamMethodRegular(commonFields.value()))
                .addModifiers(PRIVATE, STATIC).build();
    }

    private MethodSpec readMethodRegular(FieldSpec value) {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec it = ParameterSpec.builder(Constants.STRING_ITERATOR, "it").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("if ($N != null)\n", value).indent()
                .addStatement(contextUtil.throwRepetitionErrorStatement(token))
                .unindent();
        code.addStatement("$N = $N($N, $N)", value, readOptionArgumentMethod.get(), token, it);
        if (unixClustering.unixClusteringSupported()) {
            code.addStatement("return null");
        }
        return MethodSpec.methodBuilder("read")
                .addException(ExToken.class)
                .addCode(code.build())
                .returns(unixClustering.readMethodReturnType())
                .addParameters(List.of(token, it)).build();
    }

    private MethodSpec streamMethodRegular(FieldSpec value) {
        ParameterizedTypeName streamOfString = ParameterizedTypeName.get(Stream.class, String.class);
        return MethodSpec.methodBuilder("stream")
                .returns(streamOfString)
                .addStatement("return $T.ofNullable($N).stream()", OPTIONAL, value)
                .build();
    }
}
