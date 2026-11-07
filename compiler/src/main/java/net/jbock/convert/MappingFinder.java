package net.jbock.convert;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Item;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.map.AutoOrEnumMapper;

@Inject
public record MappingFinder(
        AutoOrEnumMapper autoOrEnumMapper) {

    public <M extends Item> Either<ValidationFailure, Mapping<M>> findMapping(
            M item) {
        return autoOrEnumMapper.findMapping(item);
    }
}
