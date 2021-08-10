package net.jbock.convert.map;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.TypeTool;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import static net.jbock.common.Constants.STRING;

@ValidateScope
public class AutoMappings {

    private static final String NEW = "new";
    private static final String CREATE = "create";
    private static final String VALUE_OF = "valueOf";
    private static final String COMPILE = "compile";
    private static final String PARSE = "parse";

    private final TypeTool tool;
    private final List<AutoConversion> conversions;

    @Inject
    AutoMappings(TypeTool tool) {
        this.tool = tool;
        this.conversions = autoConversions();
    }

    <M extends AnnotatedMethod>
    Optional<Mapping<M>> findAutoMapping(
            Match<M> match) {
        TypeMirror baseType = match.baseType();
        for (AutoConversion conversion : conversions) {
            if (tool.isSameType(baseType, conversion.qualifiedName())) {
                CodeBlock code = conversion.code();
                boolean multiline = conversion.multiline();
                return Optional.of(Mapping.create(code, match, multiline));
            }
        }
        return Optional.empty();
    }

    private AutoConversion create(Class<?> autoType, String methodName) {
        return create(autoType, CodeBlock.of("$T::" + methodName, autoType));
    }

    private AutoConversion create(Class<?> autoType, CodeBlock mapExpr) {
        return create(autoType, CodeBlock.of("$T.create($L)", StringConverter.class, mapExpr), false);
    }

    private AutoConversion create(
            Class<?> autoType,
            CodeBlock code,
            boolean multiline) {
        String canonicalName = autoType.getCanonicalName();
        return new AutoConversion(canonicalName, code, multiline);
    }

    private List<AutoConversion> autoConversions() {
        return List.of(
                create(String.class, CodeBlock.of("$T.identity()", Function.class)),
                create(Integer.class, VALUE_OF),
                create(Path.class, CodeBlock.of("$T::get", Paths.class)),
                create(File.class, fileConversionCode(), true),
                create(URI.class, CREATE),
                create(Pattern.class, COMPILE),
                create(LocalDate.class, PARSE),
                create(Long.class, VALUE_OF),
                create(Short.class, VALUE_OF),
                create(Byte.class, VALUE_OF),
                create(Float.class, VALUE_OF),
                create(Double.class, VALUE_OF),
                create(Character.class, charConversionCode(), true),
                create(BigInteger.class, NEW),
                create(BigDecimal.class, NEW));
    }

    private CodeBlock charConversionCode() {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        return CodeBlock.builder()
                .add("if ($N.length() != 1)\n", token).indent()
                .addStatement("throw new $T($S + $N + $S)", RuntimeException.class,
                        "Not a single character: <", token, ">").unindent()
                .addStatement("return $N.charAt(0)", token)
                .build();
    }

    private CodeBlock fileConversionCode() {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec file = ParameterSpec.builder(File.class, "file").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.addStatement("$T $N = new $T($N)", File.class, file, File.class, token);
        code.add("if (!$N.exists())\n", file).indent()
                .addStatement("throw new $T($S + $N)", IllegalStateException.class,
                        "File does not exist: ", token)
                .unindent();
        code.add("if (!$N.isFile())\n", file).indent()
                .addStatement("throw new $T($S + $N)", IllegalStateException.class,
                        "Not a file: ", token)
                .unindent();
        code.addStatement("return $N", file);
        return code.build();
    }
}
