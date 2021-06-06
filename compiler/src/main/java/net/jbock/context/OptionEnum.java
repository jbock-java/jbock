package net.jbock.context;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import net.jbock.common.EnumName;
import net.jbock.common.SafeElements;
import net.jbock.convert.Mapped;
import net.jbock.parameter.AbstractItem;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.List;

import static com.squareup.javapoet.ParameterSpec.builder;
import static com.squareup.javapoet.TypeSpec.anonymousClassBuilder;
import static javax.lang.model.element.Modifier.PRIVATE;
import static net.jbock.common.Constants.STRING_ARRAY;

/**
 * Defines the *_Parser.Option enum.
 *
 * @see GeneratedClass
 */
@ContextScope
public class OptionEnum {

  private final AllItems context;
  private final FieldSpec descriptionField;
  private final SafeElements elements;
  private final SourceElement sourceElement;

  @Inject
  OptionEnum(
      AllItems context,
      SafeElements elements,
      SourceElement sourceElement) {
    this.context = context;
    this.elements = elements;
    this.sourceElement = sourceElement;
    this.descriptionField = FieldSpec.builder(STRING_ARRAY, "description").build();
  }

  TypeSpec define() {
    List<Mapped<? extends AbstractItem>> parameters = context.items();
    TypeSpec.Builder spec = TypeSpec.enumBuilder(sourceElement.itemType());
    for (Mapped<? extends AbstractItem> param : parameters) {
      EnumName enumName = param.enumName();
      String enumConstant = enumName.enumConstant();
      CodeBlock description = descriptionBlock(param.item().description(elements));
      TypeSpec optionSpec = anonymousClassBuilder(description).build();
      spec.addEnumConstant(enumConstant, optionSpec);
    }
    return spec.addModifiers(PRIVATE)
        .addField(descriptionField)
        .addMethod(privateConstructor())
        .build();
  }

  private CodeBlock descriptionBlock(List<String> lines) {
    CodeBlock.Builder code = CodeBlock.builder();
    for (int i = 0; i < lines.size(); i++) {
      code.add("$S", lines.get(i));
      if (i != lines.size() - 1) {
        code.add(",\n");
      }
    }
    return code.build();

  }

  private MethodSpec privateConstructor() {
    ParameterSpec description = builder(ArrayTypeName.of(String.class), "description").build();
    return MethodSpec.constructorBuilder()
        .addStatement("this.$N = $N", descriptionField, description)
        .addParameter(description)
        .varargs(true)
        .build();
  }
}
