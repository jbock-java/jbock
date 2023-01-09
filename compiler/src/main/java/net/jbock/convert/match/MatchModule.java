package net.jbock.convert.match;

import dagger.Module;
import dagger.Provides;

import java.util.Set;

@Module
public interface MatchModule {

    @Provides
    static Set<Matcher> matchers(OptionalMatcher optionalMatcher, ListMatcher listMatcher) {
        return Set.of(optionalMatcher, listMatcher);
    }
}
