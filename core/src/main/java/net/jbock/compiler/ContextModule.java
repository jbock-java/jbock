package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.convert.Util;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.CommonFields;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;

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

  @Provides
  SourceElement sourceElement() {
    return sourceElement;
  }

  @Provides
  Elements elements() {
    return elements;
  }

  @Reusable
  @Provides
  PositionalParameters positionalParameters() {
    return PositionalParameters.create(params.positionalParams());
  }

  @Reusable
  @Provides
  NamedOptions namedOptions() {
    return NamedOptions.create(params.namedOptions());
  }

  @Reusable
  @Provides
  AllParameters allParameters(Util util) {
    return AllParameters.create(params, util);
  }

  @Reusable
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
