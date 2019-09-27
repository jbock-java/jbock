package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.OptionalInt;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalOptionalTest {

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      Optional<CanonicalOptional> opt = CanonicalOptional.unwrap(optionalInt, new TypeTool(elements, types));
      assertTrue(opt.isPresent());
      TypeMirror liftedType = opt.get().canonicalType();
      assertSameType("java.util.Optional<java.lang.Integer>", liftedType, elements, types);
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      Optional<CanonicalOptional> opt = CanonicalOptional.unwrap(primitiveInt, new TypeTool(elements, types));
      assertFalse(opt.isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      Optional<CanonicalOptional> opt = CanonicalOptional.unwrap(string, new TypeTool(elements, types));
      assertFalse(opt.isPresent());
    });
  }
}
