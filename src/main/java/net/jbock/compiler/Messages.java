package net.jbock.compiler;

import com.squareup.javapoet.AnnotationSpec;

import javax.annotation.Generated;
import javax.lang.model.util.Elements;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

public final class Messages {

  public static final class JavadocMessages {

    public static final String GENERATED_COMMENTS = "https://github.com/h908714124/jbock";

    static List<AnnotationSpec> generatedAnnotations(Elements elements) {
      if (elements.getTypeElement("javax.annotation.Generated") != null) {
        return singletonList(AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", Processor.class.getName())
            .addMember("comments", "$S", GENERATED_COMMENTS)
            .build());
      }
      return Collections.emptyList();

    }

    private JavadocMessages() {
      throw new UnsupportedOperationException();
    }
  }

  private Messages() {
    throw new UnsupportedOperationException();
  }
}
