package net.jbock.convert.matching;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.convert.matcher.ListMatcher;
import net.jbock.convert.matcher.Matcher;
import net.jbock.convert.matcher.OptionalMatcher;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

import static net.jbock.common.TypeTool.AS_PRIMITIVE;

@ValidateScope
public class MatchFinder {

    private final OptionalMatcher optionalMatcher;
    private final ListMatcher listMatcher;
    private final Types types;

    @Inject
    MatchFinder(
            OptionalMatcher optionalMatcher,
            ListMatcher listMatcher,
            Types types) {
        this.optionalMatcher = optionalMatcher;
        this.listMatcher = listMatcher;
        this.types = types;
    }

    public <M extends AnnotatedMethod> Match findMatch(SourceMethod<M> parameter) {
        for (Matcher matcher : List.of(optionalMatcher, listMatcher)) {
            Optional<Match> match = matcher.tryMatch(parameter);
            if (match.isPresent()) {
                return match.orElseThrow();
            }
        }
        return Match.create(AS_PRIMITIVE.visit(parameter.returnType())
                .map(types::boxedClass)
                .map(TypeElement::asType)
                .orElse(parameter.returnType()), Multiplicity.REQUIRED);
    }
}
