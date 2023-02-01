package net.jbock.validate;

import io.jbock.simple.Component;
import io.jbock.simple.Named;
import io.jbock.simple.Provides;
import io.jbock.util.Either;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.match.Matcher;
import net.jbock.processor.SourceElement;
import net.jbock.writing.CommandRepresentation;

import java.util.List;
import java.util.Set;

@Component
public interface ValidateComponent {

    CommandProcessor commandProcessor();

    @Component.Factory
    interface Factory {
        ValidateComponent create(
                Util util,
                TypeTool tool,
                SourceElement sourceElement);
    }

    static Either<List<ValidationFailure>, CommandRepresentation> generate(
            Util util,
            TypeTool tool,
            SourceElement sourceElement) {
        ValidateComponent component = ValidateComponent_Impl.factory().create(util, tool, sourceElement);
        return component.commandProcessor().generate();
    }

    @Provides
    static Set<Matcher> allMatchers(
            @Named("optional") Matcher optionalMatcher,
            @Named("list") Matcher listMatcher) {
        return Set.of(optionalMatcher, listMatcher);
    }
}
