## Running examples with Java 9

There seems to be a problem with the maven-compiler-plugin.
Apparently it's not picking up the annotation processor.

But theres a workaround:

````bash
mvn clean install -f ../core/pom.xml
mvn clean
./generate_sources
mvn test
````
