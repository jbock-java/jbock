package net.jbock.context;

import dagger.Module;
import dagger.Provides;
import net.jbock.common.SafeElements;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.Mapped;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.PositionalParameter;
import net.jbock.processor.SourceElement;

import javax.lang.model.util.Types;
import java.util.List;

@Module
public class ContextModule {

  private final SourceElement sourceElement;
  private final SafeElements elements;
  private final List<Mapped<PositionalParameter>> positionalParams;
  private final List<Mapped<NamedOption>> namedOptions;
  private final Types types;

  public ContextModule(
      SourceElement sourceElement,
      SafeElements elements,
      List<Mapped<PositionalParameter>> positionalParams,
      List<Mapped<NamedOption>> namedOptions,
      Types types) {
    this.sourceElement = sourceElement;
    this.elements = elements;
    this.positionalParams = positionalParams;
    this.namedOptions = namedOptions;
    this.types = types;
  }

  @ContextScope
  @Provides
  SourceElement sourceElement() {
    return sourceElement;
  }

  @ContextScope
  @Provides
  SafeElements elements() {
    return elements;
  }

  @ContextScope
  @Provides
  PositionalParameters positionalParameters() {
    return PositionalParameters.create(positionalParams);
  }

  @ContextScope
  @Provides
  NamedOptions namedOptions() {
    return NamedOptions.create(namedOptions);
  }

  @ContextScope
  @Provides
  Util util() {
    return new Util(types, new TypeTool(elements, types));
  }

  @ContextScope
  @Provides
  AllItems allParameters(Util util) {
    return AllItems.create(positionalParams, namedOptions, util);
  }

  @ContextScope
  @Provides
  CommonFields commonFields(
      GeneratedTypes generatedTypes,
      SourceElement sourceElement,
      PositionalParameters positionalParameters,
      NamedOptions namedOptions) {
    return CommonFields.create(
        generatedTypes,
        sourceElement,
        positionalParameters,
        namedOptions);
  }
}
