package net.jbock.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import net.jbock.Command;
import net.jbock.common.Descriptions;
import net.jbock.common.SnakeName;
import net.jbock.common.ValidationFailure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.common.Constants.ACCESS_MODIFIERS;

public class SourceElement {

    private final TypeElement sourceElement;
    private final List<Modifier> accessModifiers;
    private final String programName;
    private final ClassName generatedClass;
    private final ClassName optionEnumType;
    private final Command command;

    private SourceElement(
            TypeElement sourceElement,
            List<Modifier> accessModifiers,
            String programName,
            ClassName generatedClass,
            ClassName optionEnumType,
            Command command) {
        this.sourceElement = sourceElement;
        this.accessModifiers = accessModifiers;
        this.programName = programName;
        this.generatedClass = generatedClass;
        this.optionEnumType = optionEnumType;
        this.command = command;
    }

    static SourceElement create(TypeElement typeElement) {
        List<Modifier> accessModifiers = typeElement.getModifiers().stream()
                .filter(ACCESS_MODIFIERS::contains)
                .collect(Collectors.toUnmodifiableList());
        Command command = typeElement.getAnnotation(Command.class);
        String programName = Descriptions.optionalString(command.name())
                .orElseGet(() -> SnakeName.create(typeElement.getSimpleName().toString()).snake('-'));
        String generatedClassName = String.join("_", ClassName.get(typeElement).simpleNames()) + "Parser";
        ClassName generatedClass = ClassName.get(typeElement)
                .topLevelClassName()
                .peerClass(generatedClassName);
        ClassName optionEnumType = generatedClass.nestedClass("Opt");
        return new SourceElement(typeElement, accessModifiers,
                programName, generatedClass, optionEnumType, command);
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
        return command.superCommand();
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
        return Descriptions.optionalString(command.descriptionKey());
    }

    public List<String> description() {
        return List.of(command.description());
    }

    public boolean unixClustering() {
        return command.unixClustering();
    }

    public boolean generateParseOrExitMethod() {
        return command.generateParseOrExitMethod();
    }
}
