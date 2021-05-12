package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.AllParameters;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.NamedOptions;
import net.jbock.qualifier.PositionalParameters;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.UnixClustering;

import javax.lang.model.util.Elements;
import java.util.List;

@Module
public class ContextModule {

  private final SourceElement sourceElement;
  private final GeneratedType generatedType;
  private final Elements elements;
  private final Params params;

  public ContextModule(
      SourceElement sourceElement,
      GeneratedType generatedType,
      Elements elements,
      Params params) {
    this.sourceElement = sourceElement;
    this.generatedType = generatedType;
    this.elements = elements;
    this.params = params;
  }

  @Provides
  SourceElement sourceElement() {
    return sourceElement;
  }

  @Provides
  GeneratedType generatedType() {
    return generatedType;
  }

  @Provides
  List<ConvertedParameter<NamedOption>> namedOptions() {
    return params.namedOptions();
  }

  @Provides
  List<ConvertedParameter<PositionalParameter>> positionalParameters() {
    return params.positionalParams();
  }

  @Reusable
  @Provides
  DescriptionBuilder descriptionBuilder() {
    return new DescriptionBuilder(elements);
  }

  @Reusable
  @Provides
  Description description(DescriptionBuilder descriptionBuilder) {
    return descriptionBuilder.getDescription(sourceElement.element());
  }

  @Reusable
  @Provides
  UnixClustering unixClustering(List<ConvertedParameter<NamedOption>> namedOptions) {
    return UnixClustering.create(namedOptions);
  }

  @Reusable
  @Provides
  PositionalParameters positionals() {
    return PositionalParameters.create(params.positionalParams());
  }

  @Reusable
  @Provides
  NamedOptions options() {
    return NamedOptions.create(params.namedOptions());
  }

  @Reusable
  @Provides
  AllParameters allParameters() {
    return AllParameters.create(params);
  }
}
