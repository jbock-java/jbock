# jBock

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.h908714124/jbock)

Annotation processor that allows binding of command line options to instance fields.

The goal is to easily define GNU-style command line interface, via annotations.

This documentation will be extended over time. Meanwhile, check out the examples folder, and 
this [real-life example](https://github.com/h908714124/aws-glacier-multipart-upload).

### Caveats

jBock doesn't handle any argument conversions. Only arguments of type `String` and `boolean` may be declared.
A good place for conversion, such as `Integer.parseInt`, would be inside your annotated constructor.
