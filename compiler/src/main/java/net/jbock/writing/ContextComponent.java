package net.jbock.writing;

import dagger.BindsInstance;
import dagger.Component;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import java.util.List;

@WritingScope
@Component(modules = ContextModule.class)
public interface ContextComponent {

    ParserClass parserClass();

    ImplClass implClass();

    static ContextComponent.Builder builder() {
        return DaggerContextComponent.builder();
    }

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder sourceElement(SourceElement sourceElement);

        @BindsInstance
        Builder repeatablePositionalParameters(List<Mapping<AnnotatedParameters>> repeatablePositionalParameters);

        @BindsInstance
        Builder positionalParams(List<Mapping<AnnotatedParameter>> positionalParams);

        @BindsInstance
        Builder namedOptions(List<Mapping<AnnotatedOption>> namedOptions);

        ContextComponent build();
    }
}
