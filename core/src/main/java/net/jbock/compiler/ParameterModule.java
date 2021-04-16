package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.coerce.matching.matcher.ExactMatcher;
import net.jbock.coerce.matching.matcher.ListMatcher;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.coerce.matching.matcher.OptionalMatcher;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.qualifier.BundleKey;
import net.jbock.qualifier.MapperClass;
import net.jbock.qualifier.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

@Module
public class ParameterModule {

  private final TypeElement sourceElement;
  private final Optional<TypeElement> mapperClass;
  private final String bundleKey;

  ParameterModule(TypeElement sourceElement, Optional<TypeElement> mapperClass, String bundleKey) {
    this.sourceElement = sourceElement;
    this.mapperClass = mapperClass;
    this.bundleKey = bundleKey;
  }

  @Provides
  @SourceElement
  TypeElement sourceElement() {
    return sourceElement;
  }

  @Provides
  @MapperClass
  Optional<TypeElement> mapperClass() {
    return mapperClass;
  }

  @Provides
  @BundleKey
  public String getBundleKey() {
    return bundleKey;
  }

  @Reusable
  @Provides
  EnumName enumName(ExecutableElement sourceMethod, ImmutableList<NamedOption> alreadyCreated) {
    String methodName = sourceMethod.getSimpleName().toString();
    EnumName result = EnumName.create(methodName);
    for (Parameter param : alreadyCreated) {
      if (param.enumName().enumConstant().equals(result.enumConstant())) {
        return result.append(Integer.toString(alreadyCreated.size()));
      }
    }
    return result;
  }

  @Reusable
  @Provides
  ImmutableList<Matcher> getMatchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }
}
