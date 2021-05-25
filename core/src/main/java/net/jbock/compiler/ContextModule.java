package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import net.jbock.convert.Util;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;
import net.jbock.scope.ContextScope;

import javax.lang.model.util.Elements;

@Module
public class ContextModule {

  private final SourceElement sourceElement;
  private final Elements elements;
  private final Params params;

  public ContextModule(
      SourceElement sourceElement,
      Elements elements,
      Params params) {
    this.sourceElement = sourceElement;
    this.elements = elements;
    this.params = params;
  }

  @ContextScope
  @Provides
  SourceElement sourceElement() {
    return sourceElement;
  }

  @ContextScope
  @Provides
  Elements elements() {
    return elements;
  }

  @ContextScope
  @Provides
  PositionalParameters positionalParameters() {
    return PositionalParameters.create(params.positionalParams());
  }

  @ContextScope
  @Provides
  NamedOptions namedOptions() {
    return NamedOptions.create(params.namedOptions());
  }

  @ContextScope
  @Provides
  Util util() {
    return new Util();
  }

  @ContextScope
  @Provides
  AllParameters allParameters(Util util) {
    return AllParameters.create(params, util);
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
