package net.jbock.convert.map;

import io.jbock.javapoet.CodeBlock;
import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;
import net.jbock.contrib.StandardConverters;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;

import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

public final class AutoMappings {

    private final TypeTool tool;

    @Inject
    public AutoMappings(TypeTool tool) {
        this.tool = tool;
    }

    <M extends Item>
    Optional<Mapping<M>> findAutoMapping(
            Match<M> match) {
        TypeMirror baseType = match.baseType();
        if (tool.isSameType(baseType, String.class)) {
            return Optional.of(createMapping("asString", match));
        }
        if (tool.isSameType(baseType, Integer.class)) {
            return Optional.of(createMapping("asInteger", match));
        }
        if (tool.isSameType(baseType, Path.class)) {
            return Optional.of(createMapping("asPath", match));
        }
        if (tool.isSameType(baseType, File.class)) {
            return Optional.of(createMapping("asExistingFile", match));
        }
        if (tool.isSameType(baseType, URI.class)) {
            return Optional.of(createMapping("asURI", match));
        }
        if (tool.isSameType(baseType, Pattern.class)) {
            return Optional.of(createMapping("asPattern", match));
        }
        if (tool.isSameType(baseType, LocalDate.class)) {
            return Optional.of(createMapping("asLocalDate", match));
        }
        if (tool.isSameType(baseType, Long.class)) {
            return Optional.of(createMapping("asLong", match));
        }
        if (tool.isSameType(baseType, Short.class)) {
            return Optional.of(createMapping("asShort", match));
        }
        if (tool.isSameType(baseType, Byte.class)) {
            return Optional.of(createMapping("asByte", match));
        }
        if (tool.isSameType(baseType, Float.class)) {
            return Optional.of(createMapping("asFloat", match));
        }
        if (tool.isSameType(baseType, Double.class)) {
            return Optional.of(createMapping("asDouble", match));
        }
        if (tool.isSameType(baseType, Character.class)) {
            return Optional.of(createMapping("asCharacter", match));
        }
        if (tool.isSameType(baseType, BigInteger.class)) {
            return Optional.of(createMapping("asBigInteger", match));
        }
        if (tool.isSameType(baseType, BigDecimal.class)) {
            return Optional.of(createMapping("asBigDecimal", match));
        }
        return Optional.empty();
    }

    private static <M extends Item> Mapping<M> createMapping(
            String methodName,
            Match<M> match) {
        CodeBlock createConverterExpression = CodeBlock.of("$T.$L()", StandardConverters.class, methodName);
        return Mapping.create(createConverterExpression, match);
    }
}
