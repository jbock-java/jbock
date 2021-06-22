package net.jbock.context;

import com.squareup.javapoet.TypeName;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.squareup.javapoet.TypeName.VOID;
import static net.jbock.common.Constants.STRING;

public class NamedOptions {

  private final List<Mapped<NamedOption>> options;
  private final boolean anyRepeatable;
  private final boolean anyRegular; // any (optional|required) ?
  private final boolean anyFlags;
  private final boolean unixClusteringSupported;

  private NamedOptions(
      List<Mapped<NamedOption>> options,
      boolean anyRepeatable,
      boolean anyRegular,
      boolean anyFlags,
      boolean unixClusteringSupported) {
    this.options = options;
    this.anyRepeatable = anyRepeatable;
    this.anyRegular = anyRegular;
    this.anyFlags = anyFlags;
    this.unixClusteringSupported = unixClusteringSupported;
  }

  static NamedOptions create(List<Mapped<NamedOption>> options, boolean unixClustering) {
    boolean anyRepeatable = options.stream().anyMatch(Mapped::isRepeatable);
    boolean anyRegular = options.stream().anyMatch(option -> option.isOptional() || option.isRequired());
    boolean anyFlags = options.stream().anyMatch(Mapped::isFlag);
    return new NamedOptions(options, anyRepeatable, anyRegular, anyFlags,
        unixClustering && hasEnoughUnixNames(options));
  }

  private static boolean hasEnoughUnixNames(List<Mapped<NamedOption>> options) {
    List<Mapped<NamedOption>> unixOptions = options.stream()
        .filter(option -> option.item().hasUnixName())
        .collect(Collectors.toUnmodifiableList());
    return unixOptions.size() >= 2 && unixOptions.stream().anyMatch(Mapped::isFlag);
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

  List<Mapped<NamedOption>> options() {
    return options;
  }

  boolean isEmpty() {
    return options.isEmpty();
  }

  Stream<Mapped<NamedOption>> stream() {
    return options.stream();
  }

  boolean unixClusteringSupported() {
    return unixClusteringSupported;
  }

  TypeName readMethodReturnType() {
    return unixClusteringSupported ? STRING : VOID;
  }
}
