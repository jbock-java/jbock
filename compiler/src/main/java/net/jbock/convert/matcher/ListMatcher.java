package net.jbock.convert.matcher;

import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.convert.matching.Match;
import net.jbock.model.Multiplicity;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

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
    public <M extends AnnotatedMethod> Optional<Match<M>> tryMatch(
            M parameter) {
        TypeMirror returnType = parameter.returnType();
        return elements.getTypeElement("java.util.List")
                .flatMap(utilList -> tool.getSingleTypeArgument(returnType, utilList)
                        .map(typeArg -> Match.create(typeArg, Multiplicity.REPEATABLE, parameter)));
    }
}
