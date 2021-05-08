package net.jbock.coerce;

import com.google.common.collect.ImmutableList;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Lazy;
import net.jbock.coerce.matching.auto.AutoCoercionFinder;
import net.jbock.coerce.matching.mapper.ExplicitCoercionFinder;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.EnumName;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.either.Either;
import net.jbock.qualifier.ConverterClass;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
public class BasicInfo extends ParameterScoped {

  private final Lazy<AutoCoercionFinder> autoMatcher;
  private final ConverterClass mapperClass;
  private final ImmutableList<Matcher> matchers;

  @Inject
  BasicInfo(
      ParameterContext context,
      Lazy<AutoCoercionFinder> autoMatcher,
      ConverterClass mapperClass,
      ImmutableList<Matcher> matchers) {
    super(context);
    this.autoMatcher = autoMatcher;
    this.mapperClass = mapperClass;
    this.matchers = matchers;
  }

  @Component
  interface ParameterWithMapperComponent {

    ExplicitCoercionFinder mapperMatcher();

    @Component.Builder
    interface Builder {

      @BindsInstance
      Builder parameterContext(ParameterContext parameterContext);

      @BindsInstance
      Builder mapperClass(TypeElement mapperClass);

      @BindsInstance
      Builder matchers(ImmutableList<Matcher> matchers);

      @BindsInstance
      Builder enumName(EnumName enumName);

      @BindsInstance
      Builder typeTool(TypeTool typeTool);

      ParameterWithMapperComponent build();
    }
  }

  public <P extends AbstractParameter> Either<String, Coercion<P>> coercion(P parameter) {
    return mapperClass.converter()
        .map(mapper -> {
          ParameterWithMapperComponent component = DaggerBasicInfo_ParameterWithMapperComponent.builder()
              .parameterContext(parameterContext())
              .mapperClass(mapper)
              .matchers(matchers)
              .typeTool(tool())
              .enumName(enumName())
              .build();
          return component.mapperMatcher().findCoercion(parameter);
        })
        .orElseGet(() -> autoMatcher.get().findCoercion(parameter));
  }
}