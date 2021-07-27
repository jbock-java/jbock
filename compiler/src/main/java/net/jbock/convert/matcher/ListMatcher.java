package net.jbock.convert.matcher;

import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.convert.ConvertScope;
import net.jbock.convert.matching.Match;
import net.jbock.model.Multiplicity;
import net.jbock.source.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

@ConvertScope
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
    public Optional<Match> tryMatch(SourceMethod<?> parameter) {
        TypeMirror returnType = parameter.returnType();
        return elements.getTypeElement("java.util.List")
                .flatMap(el -> tool.getSingleTypeArgument(returnType, el)
                        .map(typeArg -> Match.create(typeArg, Multiplicity.REPEATABLE)));
    }
}
