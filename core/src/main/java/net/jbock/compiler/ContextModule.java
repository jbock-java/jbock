package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.qualifier.SourceElement;

import java.util.List;

@Module
public interface ContextModule {

  @Provides
  @Reusable
  static TypeName sourceType(SourceElement sourceElement) {
    return TypeName.get(sourceElement.element().asType());
  }

  @Provides
  @Reusable
  static List<AbstractParameter> parameters(List<PositionalParameter> params, List<NamedOption> options) {
    return ImmutableList.<AbstractParameter>builder().addAll(params).addAll(options).build();
  }

  @Provides
  @Reusable
  static GeneratedTypes generatedTypes(ClassName generatedClass, ParserFlavour flavour, SourceElement sourceElement) {
    return new GeneratedTypes(generatedClass, flavour, sourceElement);
  }
}
