package net.jbock.context;

import com.squareup.javapoet.TypeName;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.convert.Mapping;

import java.util.List;
import java.util.stream.Collectors;

import static com.squareup.javapoet.TypeName.VOID;
import static net.jbock.common.Constants.STRING;

public class NamedOptions {

    private final boolean anyRepeatable;
    private final boolean anyRegular; // any (optional|required) ?
    private final boolean anyFlags;
    private final boolean unixClusteringSupported;

    private NamedOptions(
            boolean anyRepeatable,
            boolean anyRegular,
            boolean anyFlags,
            boolean unixClusteringSupported) {
        this.anyRepeatable = anyRepeatable;
        this.anyRegular = anyRegular;
        this.anyFlags = anyFlags;
        this.unixClusteringSupported = unixClusteringSupported;
    }

    static NamedOptions create(List<Mapping<AnnotatedOption>> options, boolean unixClustering) {
        boolean anyRepeatable = options.stream().anyMatch(Mapping::isRepeatable);
        boolean anyRegular = options.stream().anyMatch(option -> option.isOptional() || option.isRequired());
        boolean anyFlags = options.stream().anyMatch(Mapping::modeFlag);
        return new NamedOptions(anyRepeatable, anyRegular, anyFlags,
                unixClustering && hasEnoughUnixNames(options));
    }

    private static boolean hasEnoughUnixNames(List<Mapping<AnnotatedOption>> options) {
        List<Mapping<AnnotatedOption>> unixOptions = options.stream()
                .filter(option -> option.sourceMethod().hasUnixName())
                .collect(Collectors.toList());
        return unixOptions.size() >= 2 && unixOptions.stream().anyMatch(Mapping::modeFlag);
    }

    boolean anyRepeatable() {
        return anyRepeatable;
    }

    boolean anyRegular() {
        return anyRegular;
    }

    boolean anyFlags() {
        return anyFlags;
    }

    boolean unixClusteringSupported() {
        return unixClusteringSupported;
    }

    TypeName readMethodReturnType() {
        return unixClusteringSupported ? STRING : VOID;
    }
}
