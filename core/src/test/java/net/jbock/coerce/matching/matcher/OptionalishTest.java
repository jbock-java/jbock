package net.jbock.coerce.matching.matcher;

import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OptionalishTest {

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      optionalish.unwrap(optionalInt).ifPresent(unwrapSuccess -> {
        TypeName liftedType = unwrapSuccess.constructorParam().type;
        assertEquals("java.util.Optional<java.lang.Integer>", liftedType.toString());
      }).orElseThrow(Assertions::fail);
    });
  }

  @Test
  void testOptionalInteger() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeElement optional = elements.getTypeElement(Optional.class.getCanonicalName());
      TypeMirror integer = elements.getTypeElement(Integer.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      DeclaredType optionalInteger = types.getDeclaredType(optional, integer);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      optionalish.unwrap(optionalInteger).ifPresent(unwrapSuccess -> {
        TypeName liftedType = unwrapSuccess.constructorParam().type;
        assertEquals("java.util.Optional<java.lang.Integer>", liftedType.toString());
      }).orElseThrow(Assertions::fail);
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      TypeTool tool = new TypeTool(elements, types);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      Assertions.assertFalse(optionalish.unwrap(primitiveInt).isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      Assertions.assertFalse(optionalish.unwrap(string).isPresent());
    });
  }
}
