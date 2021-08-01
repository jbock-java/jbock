package net.jbock.processor;

import org.junit.jupiter.api.Test;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_TYPE_ELEMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TypeToolTest {

    @Test
    void testAsTypeElement() {
        PackageElement packEl = mock(PackageElement.class);
        Optional<TypeElement> result = AS_TYPE_ELEMENT.visitPackage(packEl, null);
        assertEquals(Optional.empty(), result);
    }
}
