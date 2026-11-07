package net.jbock.convert.match;

import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;

import static net.jbock.model.Multiplicity.REQUIRED;

@Inject
public record MatchFinder(
        OptionalMatcher optionalMatcher,
        ListMatcher listMatcher,
        TypeTool tool) {

    public <M extends Item> Match<M> findMatch(M item) {
        return optionalMatcher.tryMatch(item)
                .or(() -> listMatcher.tryMatch(item))
                .orElseGet(() -> Match.create(REQUIRED, item));
    }
}
