package net.jbock.coerce.collectorabsent;

import net.jbock.coerce.collectorabsent.Optionalish;
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

class OptionalishTest {

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      Optional<Optionalish> opt = Optionalish.unwrap(optionalInt, new TypeTool(elements, types));
      assertTrue(opt.isPresent());
      TypeMirror liftedType = opt.get().liftedType();
      assertSameType("java.util.Optional<java.lang.Integer>", liftedType, elements, types);
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      Optional<Optionalish> opt = Optionalish.unwrap(primitiveInt, new TypeTool(elements, types));
      assertFalse(opt.isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      Optional<Optionalish> opt = Optionalish.unwrap(string, new TypeTool(elements, types));
      assertFalse(opt.isPresent());
    });
  }
}
