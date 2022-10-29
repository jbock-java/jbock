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
import java.util.function.Supplier;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public class SourceElement {

    private final AnyCommand command;

    private final Supplier<List<Modifier>> accessModifiers = memoize(() -> command().isPublicParser() ?
            List.of(Modifier.PUBLIC) :
            List.of());

    private final Supplier<String> programName = memoize(() -> optionalString(command().getName())
            .orElseGet(() -> SnakeName.create(command().typeElement.getSimpleName()).snake('-')));

    private final Supplier<ClassName> optionEnumType = memoize(() ->
            generatedClass().nestedClass("Opt"));

    private final Supplier<Optional<String>> descriptionKey = memoize(() ->
            optionalString(command().getDescriptionKey()));

    private final Supplier<List<String>> description = memoize(() ->
            List.of(command().getDescription()));

    private final Supplier<ClassName> generatedClass = memoize(() -> {
        ClassName commandClass = ClassName.get(command().typeElement);
        return commandClass
                .topLevelClassName()
                .peerClass(String.join("_", commandClass.simpleNames()) + "Parser");
    });

    private SourceElement(AnyCommand command) {
        this.command = command;
    }

    static SourceElement create(TypeElement t) {
        AnyCommand command = new AnyCommand(t);
        return new SourceElement(command);
    }

    private static class AnyCommand {
        final Either<Command, SuperCommand> command;
        final TypeElement typeElement;

        AnyCommand(TypeElement typeElement) {
            this.typeElement = typeElement;
            Command command = typeElement.getAnnotation(Command.class);
            this.command = command != null ?
                    left(command) :
                    right(typeElement.getAnnotation(SuperCommand.class));
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
    }

    public TypeElement element() {
        return command.typeElement;
    }

    private AnyCommand command() {
        return command;
    }

    public TypeName typeName() {
        return TypeName.get(element().asType());
    }

    public ValidationFailure fail(String message) {
        return new ValidationFailure(message, element());
    }

    public boolean isSuperCommand() {
        return command.command.isRight();
    }

    public List<Modifier> accessModifiers() {
        return accessModifiers.get();
    }

    public ClassName generatedClass() {
        return generatedClass.get();
    }

    public ClassName optionEnumType() {
        return optionEnumType.get();
    }

    public boolean isInterface() {
        return element().getKind() == ElementKind.INTERFACE;
    }

    public String programName() {
        return programName.get();
    }

    public Optional<String> descriptionKey() {
        return descriptionKey.get();
    }

    public List<String> description() {
        return description.get();
    }

    public boolean skipGeneratingParseOrExitMethod() {
        return command.isSkipGeneratingParseOrExitMethod();
    }
}
