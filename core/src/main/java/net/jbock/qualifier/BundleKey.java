package net.jbock.qualifier;

public class BundleKey {

  private final String bundleKey;

  public BundleKey(String bundleKey) {
    this.bundleKey = bundleKey;
  }

  public String key() {
    return bundleKey;
  }
}
