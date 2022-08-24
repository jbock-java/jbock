package net.jbock.processor;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.TypeName;
import net.jbock.Command;
import net.jbock.SuperCommand;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static net.jbock.common.Constants.optionalString;

public class SourceElement {

    private final TypeElement sourceElement;
    private final List<Modifier> accessModifiers;
    private final String programName;
    private final ClassName generatedClass;
    private final ClassName optionEnumType;
    private final String descriptionKey;
    private final boolean skipGeneratingParseOrExitMethod;
    private final List<String> description;
    private final boolean superCommand;

    private SourceElement(
            TypeElement sourceElement,
            List<Modifier> accessModifiers,
            String programName,
            ClassName generatedClass,
            ClassName optionEnumType,
            String descriptionKey,
            boolean skipGeneratingParseOrExitMethod,
            List<String> description,
            boolean superCommand) {
        this.sourceElement = sourceElement;
        this.accessModifiers = accessModifiers;
        this.programName = programName;
        this.generatedClass = generatedClass;
        this.optionEnumType = optionEnumType;
        this.descriptionKey = descriptionKey;
        this.skipGeneratingParseOrExitMethod = skipGeneratingParseOrExitMethod;
        this.description = description;
        this.superCommand = superCommand;
    }

    static SourceElement create(TypeElement t) {
        AnyCommand typeElement = new AnyCommand(t);
        List<Modifier> accessModifiers = typeElement.isPublicParser() ?
                List.of(Modifier.PUBLIC) :
                List.of();
        String programName = optionalString(typeElement.getName())
                .orElseGet(() -> SnakeName.create(t.getSimpleName()).snake('-'));
        String generatedClassName = String.join("_", ClassName.get(t).simpleNames()) + "Parser";
        ClassName generatedClass = ClassName.get(t)
                .topLevelClassName()
                .peerClass(generatedClassName);
        ClassName optionEnumType = generatedClass.nestedClass("Opt");
        String descriptionKey = typeElement.getDescriptionKey();
        boolean skipGeneratingParseOrExitMethod = typeElement.isSkipGeneratingParseOrExitMethod();
        List<String> description = List.of(typeElement.getDescription());
        boolean superCommand = typeElement.isSuperCommand();
        return new SourceElement(t, accessModifiers,
                programName, generatedClass, optionEnumType,
                descriptionKey, skipGeneratingParseOrExitMethod, description, superCommand);
    }

    private static class AnyCommand {
        final TypeElement typeElement;
        final Optional<Command> command;
        final Optional<SuperCommand> superCommand;

        AnyCommand(TypeElement typeElement) {
            this.typeElement = typeElement;
            this.command = Optional.ofNullable(typeElement.getAnnotation(Command.class));
            this.superCommand = Optional.ofNullable(typeElement.getAnnotation(SuperCommand.class));
        }

        String getDescriptionKey() {
            return command.map(Command::descriptionKey)
                    .or(() -> superCommand.map(SuperCommand::descriptionKey))
                    .orElseThrow();
        }

        String getName() {
            return command.map(Command::name)
                    .or(() -> superCommand.map(SuperCommand::name))
                    .orElseThrow();
        }

        boolean isPublicParser() {
            return command.map(Command::publicParser)
                    .or(() -> superCommand.map(SuperCommand::publicParser))
                    .orElseThrow();
        }

        boolean isSkipGeneratingParseOrExitMethod() {
            return command.map(Command::skipGeneratingParseOrExitMethod)
                    .or(() -> superCommand.map(SuperCommand::skipGeneratingParseOrExitMethod))
                    .orElseThrow();
        }

        String[] getDescription() {
            return command.map(Command::description)
                    .or(() -> superCommand.map(SuperCommand::description))
                    .orElseThrow();
        }

        boolean isSuperCommand() {
            return typeElement.getAnnotation(SuperCommand.class) != null;
        }
    }

    public TypeElement element() {
        return sourceElement;
    }

    public TypeName typeName() {
        return TypeName.get(sourceElement.asType());
    }

    public ValidationFailure fail(String message) {
        return new ValidationFailure(message, sourceElement);
    }

    public boolean isSuperCommand() {
        return superCommand;
    }

    public List<Modifier> accessModifiers() {
        return accessModifiers;
    }

    public ClassName generatedClass() {
        return generatedClass;
    }

    public ClassName optionEnumType() {
        return optionEnumType;
    }

    public boolean isInterface() {
        return sourceElement.getKind() == ElementKind.INTERFACE;
    }

    public String programName() {
        return programName;
    }

    public Optional<String> descriptionKey() {
        return optionalString(descriptionKey);
    }

    public List<String> description() {
        return description;
    }

    public boolean skipGeneratingParseOrExitMethod() {
        return skipGeneratingParseOrExitMethod;
    }
}
