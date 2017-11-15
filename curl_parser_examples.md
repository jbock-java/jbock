## curl parser: examples

Let's see how `CurlArguments_Parser.parse(String[] args)` handles some input.
For example, if `args` is

* `{--method, --method}`, then `method()` will return `Optional.of("--method")`.
* `{--method=}`, then `method()` will return an empty `Optional`.
* `{--method}` or `{-X}`, then `CurlArguments_Parser.parse()` will throw `IllegalArgumentException`
* `{-v, false}` then `verbose()` returns `true` and `urls()` returns a list containing a single string `"false"`.
* `{}` (an empty array), then `method()` returns an empty `Optional`, and `urls()` returns an empty list.
* `{-Xда, -XНет}` leads to `IllegalArgumentException`.
* `{-v, -v}` (repeated flag) leads to `IllegalArgumentException` as well.

