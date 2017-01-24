# AndroidUtils

A utility library for simplifying common tasks in Android development. The library is split between three modules to help reduce
unnecessary dependencies, with a `core` module and additional `databinding` and `location` modules for more specialized utilities.

## Installation

To start using the library, include the `core` module in your gradle project:

```gradle
repositories {
  maven { url 'https://github.com/acrimi/AndroidUtils/raw/master/archives/' }
  maven { url 'https://jitpack.io' }
}

dependencies {
  compile 'com.isbx:android-utils-core:0.0.8'
}
```

And in your AndroidManifest, declare your File Provider.

        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.androidtools.fileprovider"
            tools:replace="android:authorities" />

Optionally include the `databinding` or `location` modules if necessary:

```gradle
dependencies {
  compile 'com.isbx:android-utils-location:0.0.3'
  compile 'com.isbx:android-utils-databinding:0.0.2'
}
```

## Usage

More in-depth documentation is forthcoming, but for now you can check out the [javadocs](https://acrimi.github.io/AndroidUtils/).
