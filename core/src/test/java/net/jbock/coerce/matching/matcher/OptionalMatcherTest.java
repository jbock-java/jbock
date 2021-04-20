package net.jbock.coerce.matching.matcher;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParserFlavour;
import net.jbock.compiler.TypeTool;
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

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      OptionalMatcher optionalish = createMatcher(tool, optionalInt);
      optionalish.tryMatch().map(unwrapSuccess -> {
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
      optionalish.tryMatch().map(unwrapSuccess -> {
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
      Assertions.assertFalse(optionalish.tryMatch().isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      OptionalMatcher optionalish = createMatcher(tool, string);
      Assertions.assertFalse(optionalish.tryMatch().isPresent());
    });
  }

  private OptionalMatcher createMatcher(TypeTool tool, TypeMirror returnType) {
    ExecutableElement sourceMethod = Mockito.mock(ExecutableElement.class);
    Mockito.when(sourceMethod.getReturnType()).thenReturn(returnType);
    ParameterContext context = new ParameterContext(sourceMethod, null, tool, null,
        ImmutableList.of(), ImmutableList.of(), new String[0], "", EnumName.create("a"), ParserFlavour.COMMAND);
    return new OptionalMatcher(context);
  }
}
