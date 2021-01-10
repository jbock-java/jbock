package net.jbock.coerce;

import com.squareup.javapoet.ClassName;
import net.jbock.coerce.matching.AutoMatcher;
import net.jbock.coerce.matching.MapperMatcher;
import net.jbock.coerce.matching.Matcher;
import net.jbock.compiler.ParamName;
import net.jbock.compiler.TypeTool;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class CoercionProvider {

  public static Coercion nonFlagCoercion(
      ExecutableElement sourceMethod,
      TypeElement sourceElement,
      ParamName paramName,
      Optional<TypeElement> mapperClass,
      ClassName optionType,
      TypeTool tool) {
    BasicInfo info = new BasicInfo(paramName, optionType, sourceMethod, sourceElement, tool);
    return mapperClass
        .<Matcher>map(mapper -> new MapperMatcher(info, mapper))
        .orElseGet(() -> new AutoMatcher(info))
        .findCoercion();
  }
}
