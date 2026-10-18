package net.jbock.convert;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoOrEnumMapper;
import net.jbock.convert.match.Match;
import net.jbock.convert.match.MatchFinder;
import net.jbock.model.Arity;

@Inject
public record MappingFinder(
        AutoOrEnumMapper autoOrEnumMapper,
        MatchFinder matchFinder) {

    public <M extends Item> Either<ValidationFailure, Mapping<M>> findMapping(
            M item) {
        Match<M> match = matchFinder.findMatch(item);
        return autoOrEnumMapper.findMapping(match);
    }

    public <M extends Item> Mapping<M> findNullaryMapping(
            M item) {
        Match<M> match = matchFinder.createNullaryMatch(item);
        return Mapping.create(match, Arity.NULLARY);
    }
}
