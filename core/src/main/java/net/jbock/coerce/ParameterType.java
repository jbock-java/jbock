package net.jbock.coerce;

public enum ParameterType {

  REPEATABLE{
    @Override
    public boolean repeatable() {
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
  }, OPTIONAL{
    @Override
    public boolean repeatable() {
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
  }, REQUIRED{
    @Override
    public boolean repeatable() {
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
  };

  public abstract boolean repeatable();
  public abstract boolean optional();
  public abstract boolean required();
}
