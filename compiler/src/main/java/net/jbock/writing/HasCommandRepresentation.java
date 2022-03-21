package net.jbock.writing;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.FieldSpec;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

abstract class HasCommandRepresentation {

    private final CommandRepresentation commandRepresentation;

    HasCommandRepresentation(CommandRepresentation commandRepresentation) {
        this.commandRepresentation = commandRepresentation;
    }

    final List<Mapping<AnnotatedOption>> namedOptions() {
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

    final List<Mapping<AnnotatedParameter>> positionalParameters() {
        return commandRepresentation.positionalParameters();
    }

    final List<Mapping<AnnotatedParameters>> repeatablePositionalParameters() {
        return commandRepresentation.repeatablePositionalParameters();
    }

    final CommandRepresentation commandRepresentation() {
        return commandRepresentation;
    }
}
