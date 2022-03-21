package net.jbock.common;

import net.jbock.Option;
import net.jbock.Parameter;
import net.jbock.VarargsParameter;

import java.lang.annotation.Annotation;
import java.util.List;

public class Annotations {

    public static List<Class<? extends Annotation>> methodLevelAnnotations() {
        return List.of(Option.class, Parameter.class, VarargsParameter.class);
    }
}
