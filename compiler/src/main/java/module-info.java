module net.jbock.compiler {

    provides javax.annotation.processing.Processor with net.jbock.processor.JbockProcessor;

    requires auto.common;
    requires java.compiler;
    requires com.squareup.javapoet;
    requires io.jbock.util;
    requires dagger;
    requires net.jbock;
}