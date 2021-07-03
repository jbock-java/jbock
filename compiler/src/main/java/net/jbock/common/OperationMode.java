package net.jbock.common;

public enum OperationMode {

    PRODUCTION() {
        @Override
        public boolean isTest() {
            return false;
        }
    }, TEST() {
        @Override
        public boolean isTest() {
            return true;
        }
    };

    public static OperationMode valueOf(boolean test) {
        return test ? TEST : PRODUCTION;
    }

    public abstract boolean isTest();
}
