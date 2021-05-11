package net.jbock.compiler;

import com.squareup.javapoet.ClassName;
import dagger.BindsInstance;
import dagger.Component;
import net.jbock.compiler.parameter.NamedOption;
import net.jbock.compiler.parameter.PositionalParameter;
import net.jbock.compiler.view.GeneratedClass;
import net.jbock.convert.ConvertedParameter;
import net.jbock.qualifier.OptionType;

import javax.lang.model.element.TypeElement;
import java.util.List;

@Component(modules = ContextModule.class)
interface ContextComponent {

  GeneratedClass generatedClass();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder sourceElement(TypeElement sourceElement);

    @BindsInstance
    Builder generatedClass(ClassName generatedClass);

    @BindsInstance
    Builder params(List<ConvertedParameter<PositionalParameter>> parameters);

    @BindsInstance
    Builder options(List<ConvertedParameter<NamedOption>> options);

    @BindsInstance
    Builder optionType(OptionType optionType);

    @BindsInstance
    Builder flavour(ParserFlavour flavour);

    @BindsInstance
    Builder description(Description description);

    ContextComponent build();
  }
}
