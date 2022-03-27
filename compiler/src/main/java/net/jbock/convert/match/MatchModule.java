package net.jbock.convert.match;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface MatchModule {

    @Binds
    @IntoSet
    Matcher optionalMatcher(OptionalMatcher validator);

    @Binds
    @IntoSet
    Matcher listMatcher(ListMatcher validator);
}
