package net.jbock.compiler;

import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.matching.matcher.ExactMatcher;
import net.jbock.coerce.matching.matcher.ListMatcher;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.coerce.matching.matcher.OptionalMatcher;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.qualifier.BundleKey;
import net.jbock.qualifier.SourceElement;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

@Module
interface ParameterModule {

  @Reusable
  @Provides
  static EnumName enumName(ExecutableElement sourceMethod, ImmutableList<Coercion<NamedOption>> alreadyCreated) {
    String methodName = sourceMethod.getSimpleName().toString();
    EnumName result = EnumName.create(methodName);
    for (Coercion<NamedOption> param : alreadyCreated) {
      if (param.enumName().enumConstant().equals(result.enumConstant())) {
        return result.append(Integer.toString(alreadyCreated.size()));
      }
    }
    return result;
  }

  @Reusable
  @Provides
  static ImmutableList<Matcher> matchers(
      OptionalMatcher optionalMatcher,
      ListMatcher listMatcher,
      ExactMatcher exactMatcher) {
    return ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }
}
