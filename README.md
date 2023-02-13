# CSV binders for Play [![test](https://github.com/lloydmeta/play-csv/actions/workflows/test.yml/badge.svg)](https://github.com/lloydmeta/play-csv/actions/workflows/test.yml)

If you're reading this, you probably know what you want: CSV path, query and form binders for
your Play-based application.

You know why you want it:

- Smaller URLS
- Guaranteed ordering
- You want to

## SBT

### for play 2.8.x

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.6"
)
```

### for play 2.7.x

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.5"
)
```

### for play 2.6.x

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.4"
)
```

### for play 2.5.x

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.3"
)
```

### for play 2.4.x

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.2"
)
```

### for play 2.3.x

```scala
libraryDependencies ++= Seq(
    "com.beachape" %% "play-csv" % "1.1"
)
```

## How-to

To use `Csv` in your routes file, simply add the following to your `build.sbt` or `Build.scala`

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
