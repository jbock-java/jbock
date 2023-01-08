package net.jbock.writing;

import io.jbock.javapoet.TypeSpec;

import java.util.function.Supplier;

import static net.jbock.util.Suppliers.memoize;

public final class ContextComponent {

    private final Supplier<GeneratedTypes> generatedTypesProvider;
    private final Supplier<CreateModelMethod> createModelMethodProvider;
    private final Supplier<OptionStatesMethod> optionStatesMethodProvider;
    private final Supplier<ParserTypeFactory> parserTypeFactoryProvider;
    private final Supplier<ParseMethod> parseMethodProvider;
    private final Supplier<OptEnum> optEnumProvider;
    private final Supplier<ParseOrExitMethod> parseOrExitMethodProvider;
    private final Supplier<OptionNamesMethod> optionNamesMethodProvider;
    private final Supplier<ImplClass> implClassProvider;
    private final Supplier<GeneratedAnnotation> generatedAnnotationProvider;
    private final Supplier<ParserClass> parserClassProvider;

    public ContextComponent(CommandRepresentation commandRepresentation) {
        this.generatedTypesProvider = memoize(() -> new GeneratedTypes(commandRepresentation));
        this.createModelMethodProvider = memoize(() -> new CreateModelMethod(commandRepresentation));
        this.optionStatesMethodProvider = memoize(() -> new OptionStatesMethod(commandRepresentation));
        this.parserTypeFactoryProvider = memoize(() -> new ParserTypeFactory(commandRepresentation, optionStatesMethodProvider.get()));
        this.parseMethodProvider = memoize(() -> new ParseMethod(generatedTypesProvider.get(), commandRepresentation, createModelMethodProvider.get(), parserTypeFactoryProvider.get()));
        this.optEnumProvider = memoize(() -> new OptEnum(commandRepresentation));
        this.parseOrExitMethodProvider = memoize(() -> new ParseOrExitMethod(commandRepresentation, generatedTypesProvider.get(), parseMethodProvider.get(), createModelMethodProvider.get()));
        this.optionNamesMethodProvider = memoize(() -> new OptionNamesMethod(commandRepresentation));
        this.implClassProvider = memoize(() -> new ImplClass(generatedTypesProvider.get(), commandRepresentation));
        this.generatedAnnotationProvider = memoize(GeneratedAnnotation::new);
        this.parserClassProvider = memoize(() -> new ParserClass(parseMethodProvider.get(), commandRepresentation, optEnumProvider.get(), parseOrExitMethodProvider.get(), createModelMethodProvider.get(), generatedAnnotationProvider.get(), optionNamesMethodProvider.get(), optionStatesMethodProvider.get(), implClassProvider.get()));
    }

    public TypeSpec parserClass() {
        return parserClassProvider.get().define();
    }
}
