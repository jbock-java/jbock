package net.jbock.coerce;

import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.OptionalInt;

import static net.jbock.compiler.EvaluatingProcessor.assertSameType;

class LiftedTypeTest {

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      TypeMirror liftedType = LiftedType.lift(optionalInt, new TypeTool(elements, types)).liftedType();
      assertSameType("java.util.Optional<java.lang.Integer>", liftedType, elements, types);
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      TypeMirror liftedType = LiftedType.lift(primitiveInt, new TypeTool(elements, types)).liftedType();
      assertSameType("java.lang.Integer", liftedType, elements, types);
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      TypeMirror liftedType = LiftedType.lift(string, new TypeTool(elements, types)).liftedType();
      assertSameType("java.lang.String", liftedType, elements, types);
    });
  }
}
