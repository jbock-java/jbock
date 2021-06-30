package net.jbock.either;

import java.util.Optional;

abstract class UnbalancedBase<T> {

  final Optional<? extends T> value;

  UnbalancedBase(Optional<? extends T> value) {
    this.value = value;
  }
}
