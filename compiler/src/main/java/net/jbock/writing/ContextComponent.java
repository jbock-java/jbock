package net.jbock.writing;

import io.jbock.javapoet.TypeSpec;

public final class ContextComponent {

    public static TypeSpec parserClass(CommandRepresentation command) {
        GeneratedTypes generatedTypes = new GeneratedTypes(command);
        CreateModelMethod createModelMethod = new CreateModelMethod(command);
        OptionStatesMethod optionStatesMethod = new OptionStatesMethod(command);
        ParserTypeFactory parserTypeFactory = new ParserTypeFactory(command, optionStatesMethod);
        ParseMethod parseMethod = new ParseMethod(generatedTypes, command, createModelMethod, parserTypeFactory);
        OptEnum optEnum = new OptEnum(command);
        ParseOrExitMethod parseOrExitMethod = new ParseOrExitMethod(command, generatedTypes, parseMethod, createModelMethod);
        OptionNamesMethod optionNamesMethod = new OptionNamesMethod(command);
        ImplClass implClass = new ImplClass(generatedTypes, command);
        GeneratedAnnotation generatedAnnotation = new GeneratedAnnotation();
        ParserClass parserClass = new ParserClass(parseMethod, command, optEnum, parseOrExitMethod, createModelMethod,
                generatedAnnotation, optionNamesMethod, optionStatesMethod, implClass);
        return parserClass.define();
    }

    private ContextComponent() {
    }
}
