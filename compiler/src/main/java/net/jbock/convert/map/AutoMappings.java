package net.jbock.convert.map;

import com.palantir.javapoet.CodeBlock;
import net.jbock.common.TypeTool;
import net.jbock.contrib.EnumConverter;

import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

import static net.jbock.common.Constants.CONVERTERS;

public final class AutoMappings {

    private static Optional<CodeBlock> createMapping(String methodName) {
        return Optional.of(CodeBlock.of("$L.$L()", CONVERTERS, methodName));
    }

    public static Optional<CodeBlock> findAutoMapping(
            TypeTool tool,
            TypeMirror baseType) {
        if (tool.isErasureOf(baseType, Integer.class)) {
            return createMapping("asInteger");
        }
        if (tool.isErasureOf(baseType, Path.class)) {
            return createMapping("asPath");
        }
        if (tool.isErasureOf(baseType, File.class)) {
            return createMapping("asExistingFile");
        }
        if (tool.isErasureOf(baseType, URI.class)) {
            return createMapping("asURI");
        }
        if (tool.isErasureOf(baseType, Pattern.class)) {
            return createMapping("asPattern");
        }
        if (tool.isErasureOf(baseType, LocalDate.class)) {
            return createMapping("asLocalDate");
        }
        if (tool.isErasureOf(baseType, Long.class)) {
            return createMapping("asLong");
        }
        if (tool.isErasureOf(baseType, Short.class)) {
            return createMapping("asShort");
        }
        if (tool.isErasureOf(baseType, Byte.class)) {
            return createMapping("asByte");
        }
        if (tool.isErasureOf(baseType, Float.class)) {
            return createMapping("asFloat");
        }
        if (tool.isErasureOf(baseType, Double.class)) {
            return createMapping("asDouble");
        }
        if (tool.isErasureOf(baseType, Character.class)) {
            return createMapping("asCharacter");
        }
        if (tool.isErasureOf(baseType, BigInteger.class)) {
            return createMapping("asBigInteger");
        }
        if (tool.isErasureOf(baseType, BigDecimal.class)) {
            return createMapping("asBigDecimal");
        }
        return Optional.empty();
    }

    public static CodeBlock findEnumMapping(TypeMirror baseType) {
        return CodeBlock.of("$1T.create($2T::valueOf, $2T::values)",
                EnumConverter.class, baseType);
    }
}
