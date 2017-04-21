### 2017-04-14 jBock 0.1 released

* well, it's a start

### 2017-04-14 jBock 0.2 released

* add `static options()` method
* add trash

### 2017-04-14 jBock 0.3 released

* generate enum Option
* allow multiline descriptions
* allow description argument names

### 2017-04-14 jBock 0.4 released

* some javadoc on generated code
* add methods Option.describeNames, Option.descriptionBlock
* configurable indent for Option.describe

### 2017-04-18 jBock 0.6 released

* use gnu style long options --option-name=VALUE
* arguments() returns Map<Option, Argument>
* rename init(String[]) -> parse(String[]), parse() -> bind()
* allow atomic short, for example -n2

### 2017-04-20 jBock 0.7 released

* allow `List<String>`
* various changes in generated code

### 2017-04-21 jBock 0.8 released

* add `OtherTokens` annotation
* bind() declares constructor's exceptions
* rename trash() -> free() (actually a mistake, it will be renamed to otherTokens() in the next release)
