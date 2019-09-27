package net.jbock.coerce;

public enum ParameterType {

  REPEATABLE {
    @Override
    public boolean isRepeatable() {
      return true;
    }

    @Override
    public boolean optional() {
      return false;
    }

    @Override
    public boolean required() {
      return false;
    }

    @Override
    public boolean flag() {
      return false;
    }
  }, OPTIONAL {
    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean optional() {
      return true;
    }

    @Override
    public boolean required() {
      return false;
    }

    @Override
    public boolean flag() {
      return false;
    }
  }, REQUIRED {
    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean optional() {
      return false;
    }

    @Override
    public boolean required() {
      return true;
    }

    @Override
    public boolean flag() {
      return false;
    }
  }, FLAG {
    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean optional() {
      return false;
    }

    @Override
    public boolean required() {
      return false;
    }

    @Override
    public boolean flag() {
      return true;
    }
  };

  public abstract boolean isRepeatable();

  public abstract boolean optional();

  public abstract boolean required();

  public abstract boolean flag();
}
