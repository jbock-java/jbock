package net.jbock.context;

import com.squareup.javapoet.MethodSpec;

abstract class CachedMethod {

    private MethodSpec instance;

    abstract MethodSpec define();

    final MethodSpec get() {
        if (instance == null) {
            instance = define();
        }
        return instance;
    }
}
