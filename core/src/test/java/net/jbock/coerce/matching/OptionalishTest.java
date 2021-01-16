package net.jbock.coerce.matching;

import com.squareup.javapoet.TypeName;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.EvaluatingProcessor;
import net.jbock.compiler.TypeTool;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalishTest {

  @Test
  void testLiftOptionalInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror optionalInt = elements.getTypeElement(OptionalInt.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      Optional<UnwrapSuccess> opt = optionalish.unwrap(optionalInt);
      assertTrue(opt.isPresent());
      TypeName liftedType = opt.get().constructorParam().type;
      TypeMirror wrapped = opt.get().wrappedType();
      System.out.println(wrapped);
      assertEquals("java.util.Optional<java.lang.Integer>", liftedType.toString());
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
      Optional<UnwrapSuccess> opt = optionalish.unwrap(optionalInteger);
      assertTrue(opt.isPresent());
      TypeName liftedType = opt.get().constructorParam().type;
      TypeMirror wrapped = opt.get().wrappedType();
      System.out.println(wrapped);
      assertEquals("java.util.Optional<java.lang.Integer>", liftedType.toString());
    });
  }

  @Test
  void testLiftPrimitiveInt() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror primitiveInt = types.getPrimitiveType(TypeKind.INT);
      TypeTool tool = new TypeTool(elements, types);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      Optional<UnwrapSuccess> opt = optionalish.unwrap(primitiveInt);
      assertFalse(opt.isPresent());
    });
  }

  @Test
  void testLiftString() {
    EvaluatingProcessor.source().run((elements, types) -> {
      TypeMirror string = elements.getTypeElement(String.class.getCanonicalName()).asType();
      TypeTool tool = new TypeTool(elements, types);
      Optionalish optionalish = new Optionalish(tool, EnumName.create("foo"));
      Optional<UnwrapSuccess> opt = optionalish.unwrap(string);
      assertFalse(opt.isPresent());
    });
  }
}
