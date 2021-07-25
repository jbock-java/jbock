package net.jbock.convert.matcher;

import net.jbock.convert.ConvertScope;
import net.jbock.convert.matching.Match;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_PRIMITIVE;

@ConvertScope
public class ExactMatcher implements Matcher {

    private final SourceMethod sourceMethod;
    private final Types types;

    @Inject
    ExactMatcher(
            SourceMethod sourceMethod,
            Types types) {
        this.sourceMethod = sourceMethod;
        this.types = types;
    }

    @Override
    public Optional<Match> tryMatch(AbstractItem parameter) {
        Match match = Match.create(boxedReturnType(), Multiplicity.REQUIRED);
        return Optional.of(match);
    }

    private TypeMirror boxedReturnType() {
        return AS_PRIMITIVE.visit(sourceMethod.returnType())
                .map(types::boxedClass)
                .map(TypeElement::asType)
                .orElse(sourceMethod.returnType());
    }
}
