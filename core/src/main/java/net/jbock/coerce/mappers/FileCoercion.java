package net.jbock.coerce.mappers;

import java.io.File;

class FileCoercion extends SimpleCoercion {

  FileCoercion() {
    super(File.class, "new");
  }
}
