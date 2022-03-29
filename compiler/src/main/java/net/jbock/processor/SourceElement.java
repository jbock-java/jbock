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

    static SourceElement create(TypeElement typeElement) {
        List<Modifier> accessModifiers = isPublicParser(typeElement) ?
                List.of(Modifier.PUBLIC) :
                List.of();
        String programName = optionalString(getName(typeElement))
                .orElseGet(() -> SnakeName.create(typeElement.getSimpleName()).snake('-'));
        String generatedClassName = String.join("_", ClassName.get(typeElement).simpleNames()) + "Parser";
        ClassName generatedClass = ClassName.get(typeElement)
                .topLevelClassName()
                .peerClass(generatedClassName);
        ClassName optionEnumType = generatedClass.nestedClass("Opt");
        String descriptionKey = getDescriptionKey(typeElement);
        boolean skipGeneratingParseOrExitMethod = isSkipGeneratingParseOrExitMethod(typeElement);
        List<String> description = List.of(getDescription(typeElement));
        boolean superCommand = isSuperCommand(typeElement);
        return new SourceElement(typeElement, accessModifiers,
                programName, generatedClass, optionEnumType,
                descriptionKey, skipGeneratingParseOrExitMethod, description, superCommand);
    }

    private static String getDescriptionKey(TypeElement typeElement) {
        Command command = typeElement.getAnnotation(Command.class);
        if (command != null) {
            return command.descriptionKey();
        }
        return typeElement.getAnnotation(SuperCommand.class).descriptionKey();
    }

    private static String getName(TypeElement typeElement) {
        Command command = typeElement.getAnnotation(Command.class);
        if (command != null) {
            return command.name();
        }
        return typeElement.getAnnotation(SuperCommand.class).name();
    }

    private static boolean isPublicParser(TypeElement typeElement) {
        Command command = typeElement.getAnnotation(Command.class);
        if (command != null) {
            return command.publicParser();
        }
        return typeElement.getAnnotation(SuperCommand.class).publicParser();
    }

    private static boolean isSkipGeneratingParseOrExitMethod(TypeElement typeElement) {
        Command command = typeElement.getAnnotation(Command.class);
        if (command != null) {
            return command.skipGeneratingParseOrExitMethod();
        }
        return typeElement.getAnnotation(SuperCommand.class).skipGeneratingParseOrExitMethod();
    }

    private static String[] getDescription(TypeElement typeElement) {
        Command command = typeElement.getAnnotation(Command.class);
        if (command != null) {
            return command.description();
        }
        return typeElement.getAnnotation(SuperCommand.class).description();
    }

    private static boolean isSuperCommand(TypeElement typeElement) {
        Command command = typeElement.getAnnotation(Command.class);
        if (command != null) {
            return command.superCommand();
        }
        return true; // SuperCommand annotation present
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
