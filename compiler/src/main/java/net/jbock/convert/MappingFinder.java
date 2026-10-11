package net.jbock.convert;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoOrEnumMapper;
import net.jbock.convert.match.Match;
import net.jbock.convert.match.MatchFinder;
import net.jbock.model.Arity;
import net.jbock.processor.SourceElement;

@Inject
public record MappingFinder(
        AutoOrEnumMapper autoOrEnumMapper,
        SourceElement sourceElement,
        Util util,
        MatchFinder matchFinder) {

    public <M extends Item>
    Either<ValidationFailure, Mapping<M>> findMapping(M item) {
        Match<M> match = matchFinder.findMatch(item);
        return findMappingWithMatch(match);
    }

    public <M extends Item>
    Mapping<M> findNullaryMapping(M item) {
        Match<M> match = matchFinder.createNullaryMatch(item);
        return Mapping.create(match, Arity.NULLARY);
    }

    private <M extends Item>
    Either<ValidationFailure, Mapping<M>> findMappingWithMatch(Match<M> match) {
        return autoOrEnumMapper.findMapping(match);
    }
}
