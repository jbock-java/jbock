package net.jbock.writing;

import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

interface HasCommandRepresentation {

    CommandRepresentation commandRepresentation();

    default List<Mapping<Option>> namedOptions() {
        return commandRepresentation().namedOptions();
    }

    default SourceElement sourceElement() {
        return commandRepresentation().sourceElement();
    }

    default List<Mapping<Parameter>> positionalParameters() {
        return commandRepresentation().positionalParameters();
    }

    default List<Mapping<?>> allMappings() {
        return commandRepresentation().allMappings();
    }

    default boolean isSuperCommand() {
        return commandRepresentation().sourceElement().isSuperCommand();
    }

    default boolean parseOrExitMethodAcceptsList() {
        return commandRepresentation().sourceElement().parseOrExitMethodAcceptsList();
    }

    default boolean enableAtFileExpansion() {
        return commandRepresentation().sourceElement().enableAtFileExpansion();
    }
}
