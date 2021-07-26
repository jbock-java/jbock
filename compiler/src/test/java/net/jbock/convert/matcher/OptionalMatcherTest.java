package net.jbock.convert.matcher;

import net.jbock.Option;
import net.jbock.common.AnnotatedMethod;
import net.jbock.common.EnumName;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.EvaluatingProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionalMatcherTest {

    private final AbstractItem parameter = Mockito.mock(AbstractItem.class);

    @Test
    void testLiftOptionalInt() {
        EvaluatingProcessor.source().run((elements, types) -> {
            TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
            TypeTool tool = new TypeTool(new SafeElements(elements), types);
            OptionalMatcher optionalish = createMatcher(types, elements, tool, optionalInt);
            optionalish.tryMatch(parameter).map(match -> {
                TypeMirror baseType = match.baseType();
                assertEquals("java.lang.Integer", baseType.toString());
                return match;
            }).orElseGet(Assertions::fail);
        });
    }

    @Test
    void testOptionalInteger() {
        EvaluatingProcessor.source().run((elements, types) -> {
            TypeElement optional = elements.getTypeElement(java.util.Optional.class.getCanonicalName());
            TypeMirror integer = elements.getTypeElement(Integer.class.getCanonicalName()).asType();
            TypeTool tool = new TypeTool(new SafeElements(elements), types);
            DeclaredType optionalInteger = types.getDeclaredType(optional, integer);
            OptionalMatcher optionalish = createMatcher(types, elements, tool, optionalInteger);
            optionalish.tryMatch(parameter).map(match -> {
                TypeMirror baseType = match.baseType();
                assertEquals("java.lang.Integer", baseType.toString());
                return match;
            }).orElseGet(Assertions::fail);
        });
    }

    @Test
    void testLiftPrimitiveInt() {
        EvaluatingProcessor.source().run((elements, types) -> {
            TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
            TypeTool tool = new TypeTool(new SafeElements(elements), types);
            OptionalMatcher optionalish = createMatcher(types, elements, tool, primitiveInt);
            Assertions.assertFalse(optionalish.tryMatch(parameter).isPresent());
        });
    }

    @Test
    void testLiftString() {
        EvaluatingProcessor.source().run((elements, types) -> {
            TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
            TypeTool tool = new TypeTool(new SafeElements(elements), types);
            OptionalMatcher optionalish = createMatcher(types, elements, tool, string);
            Assertions.assertFalse(optionalish.tryMatch(parameter).isPresent());
        });
    }

    private OptionalMatcher createMatcher(
            Types types,
            Elements elements,
            TypeTool tool,
            TypeMirror returnType) {
        ExecutableElement sourceMethod = Mockito.mock(ExecutableElement.class);
        Option a = Mockito.mock(Option.class);
        Mockito.when(sourceMethod.getAnnotation(Mockito.any())).thenReturn(a);
        Mockito.when(sourceMethod.getReturnType()).thenReturn(returnType);
        return new OptionalMatcher(SourceMethod.create(AnnotatedMethod.create(sourceMethod, a), EnumName.create("aua"), 0),
                tool, new SafeElements(elements), types);
    }
}
