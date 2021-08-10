package net.jbock.convert.match;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.model.Multiplicity.REPEATABLE;

@ValidateScope
public class ListMatcher implements Matcher {

    private final SafeElements elements;
    private final TypeTool tool;

    @Inject
    ListMatcher(
            SafeElements elements,
            TypeTool tool) {
        this.elements = elements;
        this.tool = tool;
    }

    @Override
    public <M extends AnnotatedMethod>
    Optional<Match<M>> tryMatch(
            M sourceMethod) {
        TypeMirror returnType = sourceMethod.returnType();
        return elements.getTypeElement("java.util.List")
                .flatMap(utilList -> tool.getSingleTypeArgument(returnType, utilList)
                        .map(typeArg -> Match.create(typeArg, REPEATABLE, sourceMethod)));
    }
}
