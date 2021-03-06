[![Build Status](https://travis-ci.org/morganey-lang/let-morganey-rest.svg?branch=master)](https://travis-ci.org/morganey-lang/let-morganey-rest)
[![Build status](https://ci.appveyor.com/api/projects/status/7aa9jdpockgsuvmx/branch/master?svg=true)](https://ci.appveyor.com/project/keddelzz/let-morganey-rest/branch/master)

# Let Morganey rest

"Let Morganey rest" is an implementation of an online REPL for
[Morganey] using [Chris Done's jquery-console](https://github.com/chrisdone/jquery-console).

## How to Build

First you need to install [Morganey] locally. Go to the Morganey folder and

    $ sbt publishLocal

Then go back to Let Morganey rest folder and

    $ sbt run

## Usage

In this repository you can also find a [**documentation** of the
REST-API of `let-morganey-rest`](docs/rest-api.md).

## Contributors

(in lexicographical order)

- [keddelzz](https://github.com/keddelzz)
- [kuchenkruste](https://github.com/kuchenkruste)
- [rexim](https://github.com/rexim)

## License

See the [LICENSE](LICENSE) file for license rights and limitations (MIT).

[Morganey]: https://github.com/rexim/Morganey
