module net.jbock.compiler {

    provides javax.annotation.processing.Processor with net.jbock.processor.JbockProcessor;

    requires java.compiler;
    requires io.jbock.auto.common;
    requires io.jbock.javapoet;
    requires io.jbock.util;
    requires net.jbock;
}