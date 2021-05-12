package net.jbock.compiler;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.GeneratedType;
import net.jbock.qualifier.SourceElement;

import javax.lang.model.util.Elements;
import java.util.List;

@Module
public class ContextModule {

  private final SourceElement sourceElement;
  private final GeneratedType generatedType;
  private final Elements elements;

  public ContextModule(
      SourceElement sourceElement,
      GeneratedType generatedType,
      Elements elements) {
    this.sourceElement = sourceElement;
    this.generatedType = generatedType;
    this.elements = elements;
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
  List<ConvertedParameter<NamedOption>> namedOptions(Params params) {
    return params.namedOptions;
  }

  @Provides
  List<ConvertedParameter<PositionalParameter>> positionalParameters(Params params) {
    return params.positionalParams;
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
}
