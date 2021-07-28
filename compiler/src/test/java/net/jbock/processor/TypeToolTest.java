package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_DECLARED;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;
import static net.jbock.processor.EvaluatingProcessor.assertSameType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TypeToolTest {

    @Test
    void testToType() {
        EvaluatingProcessor.source().run((elements, types) -> {
            DeclaredType map = TypeExpr.prepare(elements, types).parse(
                    "java.util.Map<java.util.List<java.lang.String>, java.lang.String>");
            assertEquals(types.erasure(elements.getTypeElement("java.util.Map").asType()), types.erasure(map));
            assertEquals(2, map.getTypeArguments().size());
            TypeMirror key = map.getTypeArguments().get(0);
            TypeMirror value = map.getTypeArguments().get(1);
            assertSameType(
                    types.erasure(elements.getTypeElement("java.util.List").asType()),
                    types.erasure(key),
                    types);
            assertEquals(1, AS_DECLARED.visit(key).orElseThrow().getTypeArguments().size());
            assertSameType(
                    types.getDeclaredType(elements.getTypeElement("java.lang.String")),
                    AS_DECLARED.visit(key).orElseThrow().getTypeArguments().get(0),
                    types);
            assertSameType(
                    types.getDeclaredType(elements.getTypeElement("java.lang.String")),
                    value,
                    types);
        });
    }

    @Test
    void testAsTypeElement() {
        PackageElement packEl = mock(PackageElement.class);
        Optional<TypeElement> result = AS_TYPE_ELEMENT.visitPackage(packEl, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
