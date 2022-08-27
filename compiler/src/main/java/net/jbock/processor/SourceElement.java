package net.jbock.processor;

import io.jbock.javapoet.ClassName;
import io.jbock.javapoet.TypeName;
import io.jbock.util.Either;
import net.jbock.Command;
import net.jbock.SuperCommand;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
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
        AnyCommand command = new AnyCommand(t);
        List<Modifier> accessModifiers = command.isPublicParser() ?
                List.of(Modifier.PUBLIC) :
                List.of();
        String programName = optionalString(command.getName())
                .orElseGet(() -> SnakeName.create(t.getSimpleName()).snake('-'));
        ClassName optionEnumType = command.generatedClass().nestedClass("Opt");
        String descriptionKey = command.getDescriptionKey();
        boolean skipGeneratingParseOrExitMethod = command.isSkipGeneratingParseOrExitMethod();
        List<String> description = List.of(command.getDescription());
        boolean superCommand = command.isSuperCommand();
        return new SourceElement(t, accessModifiers,
                programName, command.generatedClass(), optionEnumType,
                descriptionKey, skipGeneratingParseOrExitMethod, description, superCommand);
    }

    private static class AnyCommand {
        final Either<Command, SuperCommand> command;
        final ClassName commandClass;

        AnyCommand(TypeElement typeElement) {
            Command command = typeElement.getAnnotation(Command.class);
            this.command = command != null ?
                    left(command) :
                    right(typeElement.getAnnotation(SuperCommand.class));
            this.commandClass = ClassName.get(typeElement);
        }

        String getDescriptionKey() {
            return command.fold(
                    Command::descriptionKey,
                    SuperCommand::descriptionKey);
        }

        String getName() {
            return command.fold(
                    Command::name,
                    SuperCommand::name);
        }

        boolean isPublicParser() {
            return command.fold(
                    Command::publicParser,
                    SuperCommand::publicParser);
        }

        boolean isSkipGeneratingParseOrExitMethod() {
            return command.fold(
                    Command::skipGeneratingParseOrExitMethod,
                    SuperCommand::skipGeneratingParseOrExitMethod);
        }

        String[] getDescription() {
            return command.fold(
                    Command::description,
                    SuperCommand::description);
        }

        boolean isSuperCommand() {
            return command.isRight();
        }

        ClassName generatedClass() {
            String generatedClassName = String.join("_", commandClass.simpleNames()) + "Parser";
            return commandClass
                    .topLevelClassName()
                    .peerClass(generatedClassName);
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
