package net.jbock.validate;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.convert.matcher.ExactMatcher;
import net.jbock.convert.matcher.ListMatcher;
import net.jbock.convert.matcher.Matcher;
import net.jbock.convert.matcher.OptionalMatcher;

import javax.lang.model.util.Types;
import java.util.List;

/**
 * @see ValidateScope
 */
@Module
public class ValidateModule {

    private final Types types;
    private final SafeElements elements;

    public ValidateModule(Types types, SafeElements elements) {
        this.types = types;
        this.elements = elements;
    }

    @ValidateScope
    @Provides
    Types types() {
        return types;
    }

    @ValidateScope
    @Provides
    List<Matcher> matchers(
            OptionalMatcher optionalMatcher,
            ListMatcher listMatcher,
            ExactMatcher exactMatcher) {
        return List.of(optionalMatcher, listMatcher, exactMatcher);
    }

    @ValidateScope
    @Provides
    SafeElements elements() {
        return elements;
    }
}
