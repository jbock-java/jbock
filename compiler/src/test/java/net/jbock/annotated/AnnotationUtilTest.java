package net.jbock.annotated;

import org.junit.jupiter.api.Test;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.annotated.AnnotationUtil.GET_TYPE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationUtilTest {

    @Test
    void testAsTypeElement() {
        Optional<TypeMirror> result = GET_TYPE.visitString("a", null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}