## Running examples with Java 9

Tested with Apache Maven 3.5.2.

````bash
# clean maven cache, maybe not necessary
rm -rf ~/.m2/repository/com/github/h908714124/
mvn clean install -f ../core/pom.xml
mvn clean test
````
