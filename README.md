# CSV Seq binders for Play [![Build Status](https://travis-ci.org/lloydmeta/play-csv.svg?branch=master)](https://travis-ci.org/lloydmeta/play-csv) [![Coverage Status](https://coveralls.io/repos/lloydmeta/play-csv/badge.svg)](https://coveralls.io/r/lloydmeta/play-csv)

If you're reading this, you probably know what you want: CSV path, query and form binders for
your Play application.

You know why you want it:

- Smaller URLS
- Guaranteed ordering
- You want to

## SBT

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.0"
)
```

## How-to

To use `CsvSeq` in your routes file, simply add the following to your `build.sbt` or `Build.scala`

```scala
routesImport += "com.beachape.play.Csv"
```

Then in your routes file, add routes that use `Csv`

```
GET        /                    controllers.Application.index(ids: Csv[Int])
GET        /:ids                controllers.Application.hello(ids: Csv[Int])
```

For more information, check out the project in the `sample` directory.

## Licence

The MIT License (MIT)

Copyright (c) 2015 by Lloyd Chan

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
