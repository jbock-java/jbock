package net.jbock.writing;

import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import io.jbock.simple.Inject;
import net.jbock.annotated.Option;
import net.jbock.convert.Mapping;
import net.jbock.parse.OptionState;
import net.jbock.parse.OptionStateModeFlag;
import net.jbock.parse.OptionStateNonRepeatable;
import net.jbock.parse.OptionStateRepeatable;
import net.jbock.parse.StandardParser;
import net.jbock.parse.SuperParser;
import net.jbock.parse.VarargsParameterParser;
import net.jbock.util.ExFailure;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.palantir.javapoet.ParameterSpec.builder;
import static net.jbock.common.Constants.EITHER;
import static net.jbock.common.Constants.LIST_OF_STRING;
import static net.jbock.common.Constants.STRING;
import static net.jbock.common.Constants.mapOf;
import static net.jbock.common.Suppliers.memoize;

final class ParseMethod implements HasCommandRepresentation {

    private final CommandRepresentation commandRepresentation;
    private final GeneratedTypes generatedTypes;
    private final CreateModelMethod createModelMethod;
    private final ParserTypeFactory parserTypeFactory;

    @Inject
    ParseMethod(
            GeneratedTypes generatedTypes,
            CommandRepresentation commandRepresentation,
            CreateModelMethod createModelMethod,
            ParserTypeFactory parserTypeFactory) {
        this.commandRepresentation = commandRepresentation;
        this.generatedTypes = generatedTypes;
        this.createModelMethod = createModelMethod;
        this.parserTypeFactory = parserTypeFactory;
    }

    private final Supplier<MethodSpec> define = memoize(() -> {

        ParameterSpec tokens = builder(LIST_OF_STRING, "tokens").build();

        CodeBlock.Builder code = CodeBlock.builder();

        ClassName parserType = parserTypeFactory().get();

        ParameterSpec parser = ParameterSpec.builder(parserType, "parser").build();
        ParameterSpec optionNames = ParameterSpec.builder(
                mapOf(STRING, ClassName.get(Integer.class)), "optionNames").build();
        ParameterSpec optionStates = ParameterSpec.builder(
                ArrayTypeName.of(ClassName.get(OptionState.class)),
                "optionStates").build();
        if (namedOptions().isEmpty()) {
            code.addStatement("$T $N = $T.of()", optionNames.type(), optionNames, Map.class);
        } else {
            long mapSize = namedOptions().stream()
                    .map(Mapping::item)
                    .map(Option::names)
                    .map(List::size)
                    .mapToLong(i -> i)
                    .sum();
            int capacity = (int) (1 + Math.max(mapSize * 1.35, 15));
            code.addStatement("$T $N = new $T<>($L)", optionNames.type(), optionNames, HashMap.class, capacity);
            for (Mapping<Option> namedOption : namedOptions()) {
                for (String dashedName : namedOption.item().names()) {
                    code.addStatement("$N.put($S, $L)",
                            optionNames, dashedName,
                            namedOption.item().internalIndex());
                }
            }
        }
        code.addStatement("$T $N = new $T[$L]", optionStates.type(), optionStates, OptionState.class, namedOptions().size());
        for (Mapping<Option> namedOption : namedOptions()) {
            code.addStatement("$N[$L] = new $T()",
                    optionStates, namedOption.item().internalIndex(),
                    optionParserType(namedOption));
        }
        ClassName parserClass;
        if (isSuperCommand()) {
            parserClass = ClassName.get(SuperParser.class);
        } else if (positionalParameters().stream().anyMatch(Mapping::isRepeatable)) {
            parserClass = ClassName.get(VarargsParameterParser.class);
        } else {
            parserClass = ClassName.get(StandardParser.class);
        }
        code.addStatement("$T $N = $T.create($N, $N, $L)", parserType, parser, parserClass,
                optionNames, optionStates, positionalParameters().stream().filter(m -> !m.isRepeatable()).count());
        code.add("try {\n").indent()
                .addStatement("$N.parse($N)", parser, tokens);
        code.addStatement("return $T.right(createImpl($N))", EITHER,
                parser);
        ParameterSpec ex = builder(Exception.class, "e").build();
        code.unindent().add("} catch ($T $N) {\n", ExFailure.class, ex).indent()
                .addStatement("return $T.left($N.toError($N()))",
                        EITHER, ex, createModelMethod().get())
                .unindent().add("}\n");

        return MethodSpec.methodBuilder("parse")
                .addParameter(tokens)
                .returns(generatedTypes().parseResultType())
                .addCode(code.build())
                .addModifiers(sourceElement().accessModifiers())
                .addModifiers(Modifier.STATIC)
                .build();
    });

    private ClassName optionParserType(Mapping<Option> param) {
        if (param.isRepeatable()) {
            return ClassName.get(OptionStateRepeatable.class);
        }
        if (param.isNullary()) {
            return ClassName.get(OptionStateModeFlag.class);
        }
        return ClassName.get(OptionStateNonRepeatable.class);
    }

    MethodSpec get() {
        return define.get();
    }

    private ParserTypeFactory parserTypeFactory() {
        return parserTypeFactory;
    }

    private CreateModelMethod createModelMethod() {
        return createModelMethod;
    }

    private GeneratedTypes generatedTypes() {
        return generatedTypes;
    }

    @Override
    public CommandRepresentation commandRepresentation() {
        return commandRepresentation;
    }
}
