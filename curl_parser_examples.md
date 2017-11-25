## curl parser: examples

Let's see how `CurlArguments_Parser.parse(String[] args)` handles some input.
For example, if `args` is

* `{--request, --request}`, then `method()` will return `Optional.of("--method")`.
* `{--request=}`, then `method()` will return `Optional.of("")`.
* `{--request}`, then `CurlArguments_Parser.parse()` will throw `IllegalArgumentException`
* `{-v, false}` then `verbose()` returns `true` and `urls()` returns a list containing a single string `"false"`.
* `{}` (an empty array), then `method()` returns an `Optional.empty`, and `urls()` returns an empty list.
* `{-Xда, -XНет}` leads to `IllegalArgumentException`.
* `{-v, -v}` leads to `IllegalArgumentException` as well.
