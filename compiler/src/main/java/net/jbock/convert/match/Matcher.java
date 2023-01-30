package net.jbock.convert.match;

import io.jbock.simple.Inject;
import io.jbock.simple.Named;
import net.jbock.annotated.Item;
import net.jbock.common.TypeTool;

import java.util.Optional;

public abstract class Matcher {

    abstract <M extends Item>
    Optional<Match<M>> tryMatch(
            M parameter);

    @Inject
    @Named("list")
    public static Matcher listMatcher(TypeTool tool) {
        return new ListMatcher(tool);
    }

    @Inject
    @Named("optional")
    public static Matcher optionalMatcher(TypeTool tool) {
        return new OptionalMatcher(tool);
    }
}
