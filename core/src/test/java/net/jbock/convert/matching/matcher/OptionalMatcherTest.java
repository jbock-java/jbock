package net.jbock.convert.matching.matcher;

import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.convert.matching.MatchFactory;
import net.jbock.convert.matching.MatchFactoryAccess;
import net.jbock.qualifier.SourceMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionalMatcherTest {

  private final AbstractParameter parameter = Mockito.mock(AbstractParameter.class);

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      OptionalMatcher optionalish = createMatcher(tool, optionalInt);
      optionalish.tryMatch(parameter).map(unwrapSuccess -> {
        TypeName liftedType = unwrapSuccess.constructorParam().type;
        assertEquals("java.util.Optional<java.lang.Integer>", liftedType.toString());
        return unwrapSuccess;
      }).orElseGet(Assertions::fail);
    });
  }

  @Test
  void testOptionalInteger() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeElement optional = elements.getTypeElement(Optional.class.getCanonicalName());
      TypeMirror integer = elements.getTypeElement(Integer.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      DeclaredType optionalInteger = types.getDeclaredType(optional, integer);
      OptionalMatcher optionalish = createMatcher(tool, optionalInteger);
      optionalish.tryMatch(parameter).map(unwrapSuccess -> {
        TypeName liftedType = unwrapSuccess.constructorParam().type;
        assertEquals("java.util.Optional<java.lang.Integer>", liftedType.toString());
        return unwrapSuccess;
      }).orElseGet(Assertions::fail);
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      TypeTool tool = new TypeTool(elements, types);
      OptionalMatcher optionalish = createMatcher(tool, primitiveInt);
      Assertions.assertFalse(optionalish.tryMatch(parameter).isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      OptionalMatcher optionalish = createMatcher(tool, string);
      Assertions.assertFalse(optionalish.tryMatch(parameter).isPresent());
    });
  }

  private OptionalMatcher createMatcher(TypeTool tool, TypeMirror returnType) {
    ExecutableElement sourceMethod = Mockito.mock(ExecutableElement.class);
    Mockito.when(sourceMethod.getReturnType()).thenReturn(returnType);
    EnumName enumName = EnumName.create("a");
    MatchFactory matchFactory = MatchFactoryAccess.create(enumName);
    return new OptionalMatcher(
        SourceMethod.create(sourceMethod),
        enumName, tool, tool.types(), tool.elements(), matchFactory);
  }
}
