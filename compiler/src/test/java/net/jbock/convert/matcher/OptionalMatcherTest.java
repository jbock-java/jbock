package net.jbock.convert.matcher;

import net.jbock.Option;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.parameter.AbstractParameter;
import net.jbock.processor.EvaluatingProcessor;
import net.jbock.validate.SourceMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionalMatcherTest {

  private final AbstractParameter parameter = Mockito.mock(AbstractParameter.class);

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(new SafeElements(elements), types);
      OptionalMatcher optionalish = createMatcher(elements, tool, optionalInt);
      optionalish.tryMatch(parameter).map(unwrapSuccess -> {
        TypeMirror baseType = unwrapSuccess.baseType();
        assertEquals("java.lang.Integer", baseType.toString());
        return unwrapSuccess;
      }).orElseGet(Assertions::fail);
    });
  }

  @Test
  void testOptionalInteger() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeElement optional = elements.getTypeElement(Optional.class.getCanonicalName());
      TypeMirror integer = elements.getTypeElement(Integer.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(new SafeElements(elements), types);
      DeclaredType optionalInteger = types.getDeclaredType(optional, integer);
      OptionalMatcher optionalish = createMatcher(elements, tool, optionalInteger);
      optionalish.tryMatch(parameter).map(unwrapSuccess -> {
        TypeMirror baseType = unwrapSuccess.baseType();
        assertEquals("java.lang.Integer", baseType.toString());
        return unwrapSuccess;
      }).orElseGet(Assertions::fail);
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      TypeTool tool = new TypeTool(new SafeElements(elements), types);
      OptionalMatcher optionalish = createMatcher(elements, tool, primitiveInt);
      Assertions.assertFalse(optionalish.tryMatch(parameter).isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(new SafeElements(elements), types);
      OptionalMatcher optionalish = createMatcher(elements, tool, string);
      Assertions.assertFalse(optionalish.tryMatch(parameter).isPresent());
    });
  }

  private OptionalMatcher createMatcher(Elements elements, TypeTool tool, TypeMirror returnType) {
    ExecutableElement sourceMethod = Mockito.mock(ExecutableElement.class);
    Mockito.when(sourceMethod.getAnnotation(Mockito.any())).thenReturn(Mockito.mock(Option.class));
    Mockito.when(sourceMethod.getReturnType()).thenReturn(returnType);
    return new OptionalMatcher(SourceMethod.create(sourceMethod),
        tool, new SafeElements(elements));
  }
}
