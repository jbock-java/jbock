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
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public final class AutoMappings {

    private final TypeTool tool;
    private final List<AutoMapping> mappings;

    @Inject
    public AutoMappings(TypeTool tool) {
        this.tool = tool;
        this.mappings = autoMappings();
    }

    <M extends Item>
    Optional<Mapping<M>> findAutoMapping(
            Match<M> match) {
        TypeMirror baseType = match.baseType();
        for (AutoMapping conversion : mappings) {
            if (tool.isSameType(baseType, conversion.qualifiedName)) {
                Mapping<M> mapping = Mapping.create(conversion.createConverterExpression, match);
                return Optional.of(mapping);
            }
        }
        return Optional.empty();
    }

    private static AutoMapping create(
            Class<?> autoType,
            String methodName) {
        String canonicalName = autoType.getCanonicalName();
        CodeBlock createConverterExpression = CodeBlock.of("$T.$L()", StandardConverters.class, methodName);
        return new AutoMapping(canonicalName, createConverterExpression);
    }

    private static List<AutoMapping> autoMappings() {
        return List.of(
                create(String.class, "asString"),
                create(Integer.class, "asInteger"),
                create(Path.class, "asPath"),
                create(File.class, "asExistingFile"),
                create(URI.class, "asURI"),
                create(Pattern.class, "asPattern"),
                create(LocalDate.class, "asLocalDate"),
                create(Long.class, "asLong"),
                create(Short.class, "asShort"),
                create(Byte.class, "asByte"),
                create(Float.class, "asFloat"),
                create(Double.class, "asDouble"),
                create(Character.class, "asCharacter"),
                create(BigInteger.class, "asBigInteger"),
                create(BigDecimal.class, "asBigDecimal"));
    }

    private static final class AutoMapping {
        final String qualifiedName;
        final CodeBlock createConverterExpression;

        AutoMapping(String qualifiedName, CodeBlock createConverterExpression) {
            this.qualifiedName = qualifiedName;
            this.createConverterExpression = createConverterExpression;
        }
    }
}
