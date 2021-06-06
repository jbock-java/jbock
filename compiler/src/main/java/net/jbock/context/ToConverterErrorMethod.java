package net.jbock.context;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.model.CommandModel;
import net.jbock.util.ConverterError;
import net.jbock.util.NotSuccess;

import javax.inject.Inject;

@ContextScope
public class ToConverterErrorMethod extends Cached<MethodSpec> {

  private final CommonFields commonFields;

  @Inject
  ToConverterErrorMethod(CommonFields commonFields) {
    this.commonFields = commonFields;
  }

  @Override
  MethodSpec define() {
    ParameterSpec model = ParameterSpec.builder(CommandModel.class, "model").build();
    return MethodSpec.methodBuilder("toConverterError")
        .addParameter(model)
        .addStatement("new $T($N, $N, $N, $N)",
            ConverterError.class, model, commonFields.convExFailure(),
            commonFields.convExItemType(), commonFields.convExItemName())
        .returns(NotSuccess.class)
        .build();
  }

}
