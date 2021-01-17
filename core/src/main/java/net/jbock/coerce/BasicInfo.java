package net.jbock.coerce;

import com.google.common.collect.ImmutableList;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Lazy;
import net.jbock.coerce.matching.AutoMatcher;
import net.jbock.coerce.matching.MapperMatcher;
import net.jbock.coerce.matching.Matcher;
import net.jbock.compiler.MapperClass;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.TypeTool;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

/**
 * Coercion input: Information about a single parameter (option or param).
 */
public class BasicInfo extends ParameterScoped {

  private final Lazy<AutoMatcher> autoMatcher;
  private final Optional<TypeElement> mapperClass;
  private final ImmutableList<Matcher> matchers;

  @Inject
  BasicInfo(
      ParameterContext context,
      Lazy<AutoMatcher> autoMatcher,
      @MapperClass Optional<TypeElement> mapperClass,
      ImmutableList<Matcher> matchers) {
    super(context);
    this.autoMatcher = autoMatcher;
    this.mapperClass = mapperClass;
    this.matchers = matchers;
  }

  @Component
  interface ParameterWithMapperComponent {

    MapperMatcher mapperMatcher();

    @Component.Builder
    interface Builder {

      @BindsInstance
      Builder parameterContext(ParameterContext parameterContext);

      @BindsInstance
      Builder mapperClass(TypeElement mapperClass);

      @BindsInstance
      Builder matchers(ImmutableList<Matcher> matchers);

      @BindsInstance
      Builder typeTool(TypeTool typeTool);

      ParameterWithMapperComponent build();
    }
  }

  public Coercion coercion() {
    return mapperClass
        .map(mapper -> {
          ParameterWithMapperComponent component = DaggerBasicInfo_ParameterWithMapperComponent.builder()
              .parameterContext(parameterContext())
              .mapperClass(mapper)
              .matchers(matchers)
              .typeTool(tool())
              .build();
          return component.mapperMatcher().findCoercion();
        })
        .orElseGet(() -> autoMatcher.get().findCoercion());
  }
}