package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.compiler.parameter.PositionalParameter;

import javax.lang.model.element.TypeElement;
import java.util.List;

@Module
public interface ContextModule {

  @Provides
  @Reusable
  static TypeName sourceType(TypeElement sourceElement) {
    return TypeName.get(sourceElement.asType());
  }

  @Provides
  @Reusable
  static List<Parameter> parameters(List<PositionalParameter> params, List<NamedOption> options) {
    return ImmutableList.<Parameter>builder().addAll(params).addAll(options).build();
  }

  @Provides
  @Reusable
  static GeneratedTypes generatedTypes(ClassName generatedClass, ParserFlavour flavour, TypeElement sourceElement) {
    return new GeneratedTypes(generatedClass, flavour, sourceElement);
  }
}
