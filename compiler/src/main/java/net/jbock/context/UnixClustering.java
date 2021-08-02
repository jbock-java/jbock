package net.jbock.context;

import com.squareup.javapoet.TypeName;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;

import static com.squareup.javapoet.TypeName.VOID;
import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.STRING;

@ContextScope
public class UnixClustering {

    private final boolean unixClusteringSupported;

    @Inject
    UnixClustering(
            SourceElement sourceElement,
            List<Mapping<AnnotatedOption>> options) {
        this.unixClusteringSupported = sourceElement.unixClustering()
                && hasEnoughUnixNames(options);
    }

    private static boolean hasEnoughUnixNames(List<Mapping<AnnotatedOption>> options) {
        List<Mapping<AnnotatedOption>> unixOptions = options.stream()
                .filter(option -> option.sourceMethod().hasUnixName())
                .collect(toList());
        return unixOptions.size() >= 2 && unixOptions.stream().anyMatch(Mapping::modeFlag);
    }

    boolean unixClusteringSupported() {
        return unixClusteringSupported;
    }

    TypeName readMethodReturnType() {
        return unixClusteringSupported ? STRING : VOID;
    }
}
