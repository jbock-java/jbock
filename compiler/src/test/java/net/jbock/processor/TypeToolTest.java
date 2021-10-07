package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.common.TypeTool.ANNOTATION_VALUE_AS_TYPE;
import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TypeToolTest {

    @Test
    void testAsTypeElement() {
        PackageElement packEl = mock(PackageElement.class);
        Optional<TypeElement> result = AS_TYPE_ELEMENT.visitPackage(packEl, null);
        assertEquals(Optional.empty(), result);
    }

    @Test
    void testAnnotationValueAsType() {
        Optional<TypeMirror> result = ANNOTATION_VALUE_AS_TYPE.visitString("a", null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
