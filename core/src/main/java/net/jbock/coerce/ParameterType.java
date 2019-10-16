package net.jbock.coerce;

public enum ParameterType {

  REPEATABLE {
    @Override
    public boolean isRepeatable() {
      return true;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean isRequired() {
      return false;
    }

    @Override
    public boolean isFlag() {
      return false;
    }
  },

  OPTIONAL {
    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean isOptional() {
      return true;
    }

    @Override
    public boolean isRequired() {
      return false;
    }

    @Override
    public boolean isFlag() {
      return false;
    }
  },

  REQUIRED {
    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean isRequired() {
      return true;
    }

    @Override
    public boolean isFlag() {
      return false;
    }
  },

  FLAG {
    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public boolean isOptional() {
      return false;
    }

    @Override
    public boolean isRequired() {
      return false;
    }

    @Override
    public boolean isFlag() {
      return true;
    }
  };

  public abstract boolean isRepeatable();

  public abstract boolean isOptional();

  public abstract boolean isRequired();

  public abstract boolean isFlag();
}
