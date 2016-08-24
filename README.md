# AndroidUtils

A utility library for simplifying common tasks in Android development. The library is split between three packages to help reduce
unnecessary dependencies, with a core module and additional databinding and location modules for more specialized utilities.

## Installation

To start using the library, include the core module in your gradle project:

```gradle
repositories {
  maven { url 'https://github.com/acrimi/AndroidUtils/raw/master/archives/' }
}

dependencies {
  compile 'com.isbx:android-utils-core:0.0.2'
}
```

Optionally include the databinding or location modules if necessary:

```gradle
dependencies {
  compile 'com.isbx:android-utils-location:0.0.2'
  compile 'com.isbx:android-utils-databinding:0.0.2'
}
```

## Usage

More in-depth documentation is forthcoming, but for now you can check out the [javadocs](https://acrimi.github.io/AndroidUtils/).
