package net.jbock.writing;

import dagger.Module;
import dagger.Provides;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.annotated.AnnotatedParameter;
import net.jbock.annotated.AnnotatedParameters;
import net.jbock.convert.Mapping;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @see WritingScope
 */
@Module
interface ContextModule {

    @WritingScope
    @Provides
    static List<Mapping<?>> allMappings(
            List<Mapping<AnnotatedParameter>> positionalParams,
            List<Mapping<AnnotatedParameters>> repeatablePositionalParameters,
            List<Mapping<AnnotatedOption>> namedOptions) {
        return Stream.of(namedOptions, positionalParams, repeatablePositionalParameters)
                .flatMap(List::stream)
                .collect(toList());
    }
}
