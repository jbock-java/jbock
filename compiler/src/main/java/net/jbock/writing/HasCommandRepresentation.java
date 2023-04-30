package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.FieldSpec;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.annotated.VarargsParameter;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;
import java.util.Optional;

abstract class HasCommandRepresentation {

    private final CommandRepresentation commandRepresentation;

    HasCommandRepresentation(CommandRepresentation commandRepresentation) {
        this.commandRepresentation = commandRepresentation;
    }

    final List<Mapping<Option>> namedOptions() {
        return commandRepresentation.namedOptions();
    }

    final SourceElement sourceElement() {
        return commandRepresentation.sourceElement();
    }

    final ClassName optType() {
        return commandRepresentation.optType();
    }

    final FieldSpec optionNames() {
        return commandRepresentation.optionNames();
    }

    final List<Mapping<Parameter>> positionalParameters() {
        return commandRepresentation.positionalParameters();
    }

    final Optional<Mapping<VarargsParameter>> varargsParameter() {
        return commandRepresentation.varargsParameter();
    }

    final List<Mapping<?>> allMappings() {
        return commandRepresentation.allMappings();
    }

    final boolean isSuperCommand() {
        return commandRepresentation.sourceElement().isSuperCommand();
    }

    final boolean parseOrExitMethodAcceptsList() {
        return commandRepresentation.sourceElement().parseOrExitMethodAcceptsList();
    }

    final boolean enableAtFileExpansion() {
        return commandRepresentation.sourceElement().enableAtFileExpansion();
    }
}
