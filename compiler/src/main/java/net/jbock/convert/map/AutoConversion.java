package net.jbock.convert.map;

import io.jbock.javapoet.CodeBlock;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;

final class AutoConversion {

    private final String qualifiedName;
    private final CodeBlock mapper;

    AutoConversion(
            String qualifiedName,
            CodeBlock mapper) {
        this.qualifiedName = qualifiedName;
        this.mapper = mapper;
    }

    String qualifiedName() {
        return qualifiedName;
    }

    <M extends AnnotatedMethod>
    Mapping<M> toMapping(Match<M> match) {
        return Mapping.create(mapper, match);
    }
}
