package net.jbock.processor;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.TypeName;
import net.jbock.Command;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static net.jbock.common.Constants.optionalString;
import static net.jbock.common.Suppliers.memoize;

public final class SourceElement {

    private final AnyCommand command;

    private final Supplier<List<Modifier>> accessModifiers = memoize(() -> command().isPublicParser() ?
            List.of(Modifier.PUBLIC) :
            List.of());

    private final Supplier<String> programName = memoize(() -> optionalString(command().getName())
            .orElseGet(() -> SnakeName.create(command().typeElement.getSimpleName()).snake('-')));

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
        final Command command;
        final TypeElement typeElement;

        AnyCommand(TypeElement typeElement) {
            this.typeElement = typeElement;
            this.command = Objects.requireNonNull(typeElement.getAnnotation(Command.class));
        }

        String getDescriptionKey() {
            return command.descriptionKey();
        }

        String getName() {
            return command.name();
        }

        boolean isPublicParser() {
            return command.publicParser();
        }

        boolean isSkipGeneratingParseOrExitMethod() {
            return command.skipGeneratingParseOrExitMethod();
        }

        boolean isParseOrExitMethodAcceptsList() {
            return command.parseOrExitMethodAcceptsList();
        }

        boolean isEnableAtFileExpansion() {
            return command.enableAtFileExpansion();
        }

        boolean skipHelp() {
            return command.skipHelp();
        }

        String[] getDescription() {
            return command.description();
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
        return command.command.superCommand();
    }

    public List<Modifier> accessModifiers() {
        return accessModifiers.get();
    }

    public ClassName generatedClass() {
        return generatedClass.get();
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

    public boolean skipHelp() {
        return command.skipHelp();
    }

    public boolean parseOrExitMethodAcceptsList() {
        return command.isParseOrExitMethodAcceptsList();
    }

    public boolean enableAtFileExpansion() {
        return command.isEnableAtFileExpansion();
    }
}
