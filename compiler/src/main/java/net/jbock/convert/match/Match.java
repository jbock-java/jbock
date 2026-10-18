package net.jbock.convert.match;

import com.palantir.javapoet.CodeBlock;
import net.jbock.annotated.Item;
import net.jbock.model.Multiplicity;

import javax.lang.model.type.TypeMirror;
import java.util.Optional;

/* baseType (List<A>) == A
/* baseType (Optional<A>) == A
 * baseType (OptionalInt) == Integer
 * baseType (int) == Integer
 */
public record Match<M extends Item>(
        TypeMirror baseType,
        Multiplicity multiplicity,
        M item,
        Optional<CodeBlock> extractExpr) {

    static <M extends Item> Match<M> create(
            TypeMirror baseType,
            Multiplicity multiplicity,
            M item) {
        return new Match<>(baseType, multiplicity, item, Optional.empty());
    }
}
