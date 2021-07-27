package net.jbock.convert.matcher;

import net.jbock.convert.ConvertScope;
import net.jbock.convert.matching.Match;
import net.jbock.model.Multiplicity;
import net.jbock.parameter.AbstractItem;
import net.jbock.source.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_PRIMITIVE;

@ConvertScope
public class ExactMatcher implements Matcher {

    private final Types types;

    @Inject
    ExactMatcher(Types types) {
        this.types = types;
    }

    @Override
    public Optional<Match> tryMatch(AbstractItem parameter) {
        Match match = Match.create(boxedReturnType(parameter.sourceMethod()), Multiplicity.REQUIRED);
        return Optional.of(match);
    }

    private TypeMirror boxedReturnType(SourceMethod<?> sourceMethod) {
        return AS_PRIMITIVE.visit(sourceMethod.returnType())
                .map(types::boxedClass)
                .map(TypeElement::asType)
                .orElse(sourceMethod.returnType());
    }
}
