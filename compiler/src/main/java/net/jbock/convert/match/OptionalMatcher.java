package net.jbock.convert.match;

import io.jbock.simple.Inject;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;

import java.util.Optional;

import static net.jbock.model.Multiplicity.OPTIONAL;

@Inject
public record OptionalMatcher(TypeTool tool) {

    <M extends Item> Optional<Match<M>> tryMatch(M item) {
        if (item.isOptional()) {
            return Optional.of(Match.create(OPTIONAL, item));
        }
        return Optional.empty();
    }
}
