package net.jbock.convert.map;

import io.jbock.javapoet.CodeBlock;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.TypeTool;
import net.jbock.contrib.CharConverter;
import net.jbock.contrib.FileConverter;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

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

@ValidateScope
class AutoMappings {

    private enum FactoryMethod {
        NEW("new"),
        CREATE("create"),
        VALUE_OF("valueOf"),
        COMPILE("compile"),
        PARSE("parse");

        final String methodName;

        FactoryMethod(String methodName) {
            this.methodName = methodName;
        }

        AutoMapping create(Class<?> autoType) {
            CodeBlock mapper = CodeBlock.of("$T::" + methodName, autoType);
            return AutoMappings.wrap(autoType, mapper);
        }
    }

    private final TypeTool tool;
    private final List<AutoMapping> mappings;

    @Inject
    AutoMappings(TypeTool tool) {
        this.tool = tool;
        this.mappings = autoMappings();
    }

    <M extends AnnotatedMethod>
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

    private static AutoMapping wrap(
            Class<?> autoType,
            CodeBlock innerExpression) {
        return create(autoType, CodeBlock.of("$T.create($L)", StringConverter.class, innerExpression));
    }

    private static AutoMapping create(
            Class<?> autoType,
            CodeBlock createConverterExpression) {
        String canonicalName = autoType.getCanonicalName();
        return new AutoMapping(canonicalName, createConverterExpression);
    }

    private static List<AutoMapping> autoMappings() {
        return List.of(
                wrap(String.class, CodeBlock.of("$T.identity()", Function.class)),
                FactoryMethod.VALUE_OF.create(Integer.class),
                wrap(Path.class, CodeBlock.of("$T::get", Paths.class)),
                create(File.class, CodeBlock.of("$T.create()", FileConverter.class)),
                FactoryMethod.CREATE.create(URI.class),
                FactoryMethod.COMPILE.create(Pattern.class),
                FactoryMethod.PARSE.create(LocalDate.class),
                FactoryMethod.VALUE_OF.create(Long.class),
                FactoryMethod.VALUE_OF.create(Short.class),
                FactoryMethod.VALUE_OF.create(Byte.class),
                FactoryMethod.VALUE_OF.create(Float.class),
                FactoryMethod.VALUE_OF.create(Double.class),
                create(Character.class, CodeBlock.of("$T.create()", CharConverter.class)),
                FactoryMethod.NEW.create(BigInteger.class),
                FactoryMethod.NEW.create(BigDecimal.class));
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
