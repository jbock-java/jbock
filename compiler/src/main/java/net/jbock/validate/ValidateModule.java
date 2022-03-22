package net.jbock.validate;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import net.jbock.convert.match.ListMatcher;
import net.jbock.convert.match.Matcher;
import net.jbock.convert.match.OptionalMatcher;

@Module
public interface ValidateModule {

    @Binds
    @IntoSet
    Matcher optionalMatcher(OptionalMatcher validator);

    @Binds
    @IntoSet
    Matcher listMatcher(ListMatcher validator);
}
