# skit2demo

> Android SKIT 2.0 SDK integration demo app

An example app showcasing how to integrate the SKIT SDK into an Android application.

### About the SKIT SDK

The SKIT SDK is a Flipp-owned library used to parse and render SFML documents, features include:
- Rendering SFML documents as a view child
- Views are scrollable, zoomable and will fetch higher quality images depending on zoom length.
- Provide a `Wayfinder` side drawer, for faster page category navigation
- Analytics delegate module for capturing clicks, "engaged visits" and flyer item impressions
- An option to pass in your own image rendering/caching library

## Getting started

Clone the project

```shell
git clone https://github.com/wishabi/skit2demo
```

Obtain `username` and `password` credentials to access our jfrog artifactory

Edit the `./settings.gradle` file and insert the credentials into the username and password fields.

```
username = "{{ FLIPP_PROVIDED_USERNAME }}"
password = "{{ FLIPP_PROIVDED_PASSWORD }}"
```

Run gradle build

```shell
./gradlew build
```

Install the project on your device

```shell
./gradlew installDebug
```

## Fetching a custom SFML document

Inside of `StorefrontActivity` change the `sfmlUrlSource`.

## Difference between SKIT v1.0 and SKIT v2.0

- SKIT v1.0 was built mostly in Java, whereas in SKIT v2.0 was built (mostly) in Kotlin
- Replaced all deprecated `android.support.*` libraries in favour of `androidx.*` packages
- In terms of features, SKIT v2.0 is a superset of SKIT v1.0 that can parse out SFML2.0 documents

## Breaking changes

* `StorefrontViewBuilder#setAreaClickListener(l: SourceImage.OnAreaClickListener)` has been changed to `StorefrontViewBuilder#setAreaClickListener(l: StorefrontImageView.OnAreaClickListener)`
* `StorefrontImageView.OnAreaClickListener` is a new interface with `onAreaClicked(v: View, area: SFArea?)` and `onAreaLongPressed(v: View?, area: SFArea?)`
* `StorefrontViewBuilder#setClipStateDelegate(l: SourceImageView.ClipStateDelegate)` has been changed to `StorefrontViewBuilder#setClipStateDelegate(l: StorefrontImageView.ClipStateDelegate)`
* `StorefrontViewBuilder#setMatchupDelegate(l: SourceImageView.MatchupDelegate)` has been changed to `StorefrontViewBuilder#setMatchupDelegate(l: StorefrontImageView.MatchupDelegate)`

## More Resources

A majority of our SKIT v1.0 documentation is still valid in https://github.com/wishabi/android-skit-example/wiki

### Integrating SKIT v2.0 into an existing app



## Integrating the SDK into an existing application

Obtain credentials to access our maven repository.

In your `build.gradle` add the following

```
repositories {
    ...
    maven {
        url "https://flipplib.jfrog.io/flipplib/android-skit-local"
        credentials {
            username = "{{ FLIPP_PROVIDED_USERNAME }}"
            password = "{{ FLIPP_PROVIDED_PASSWORD }}"
        }
    }
    ...
}

...


// Both projects are required for integrating the SKIT SDK
implementation ("com.flipp.android:sfml:2.0.7")
implementation ("com.flipp.android:injectablehelper:1.1.4")
```

## Initializing the SDK

In your project's `Application` class, initialize your the SDK with an image loader of your choice.

> An `ImageLoader` is an injected module that will implement how images are rendered and cached in your rendered SFML documents. We recommend Glide as your ImageLoader, see the `GlideLoader` class as a working example in this project.

```
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HelperManager.getService(ImageLoader::class.java).setImageLoader(GlideLoader(this))
        ...
    }

    ...
}
```
## Fetching & Parsing SFML document

> Starting SKIT SDK version 2.1.6, ParseStorefrontTask which uses AsyncTask to parse an SFML document has been Deprecated.
> Different approaches are provided to help fetch & parse the sfml document from a given sfml source url based on your needs

Users migrating from any sfml version less than 2.1.6, will need to make the following changes to be able to use the recommended solution

1. Replace the callback ***ParseStorefrontTask.StorefrontLoadListener*** with ***ParseStorefrontHelper.ParseStorefrontLoadListener***

2. Replace the following block of code with the block after it & adjust the respective callbacks

    ```
    val loadingTask = SimpleSFMLLoader(this, sfmlUrlSource)
    loadingTask.execute()
   ```

   For Kotlin users, replace with below
    ```
    val helper = ParseStorefrontHelper()
    helper.fetchAndParseStorefront(lifecycleScope, sfmlUrlSource, this)    
    ```

   For Java users, replace with below
    ```
    ParseStorefrontHelper helper = new ParseStorefrontHelper();
    helper.fetchAndParseStorefront(sfmlUrlSource, this);
    ```

> The example app comes with a working example of how one might fetch & parse sfml into a Storefront object using ParseStorefrontHelper in both platforms.

## Fetching an SFML document

The SKIT SDK does not prescribe how an SFML document is retrieved, we allow our consumers to use their preferred network client tech stack.

> The example app comes with a working example of how one might do so using both executors(Java) and coroutines(Kotlin).

## Parsing an SFML document

Since SFML files can get quite large, our SDK only allows for `InputStream`s when parsing SFML documents.

> This allows for the SDK to parse SFML files whether they come from the networks, or as a persistant stored file.

To parse an SFML document InputStream to a `StoreFront` object:

1. Recommendation is to use SDK's ***ParseStorefrontHelper.parseStorefront()*** which returns the Storefront object using a callback interface
    ```
    parseStorefrontHelper.parseStorefront(lifecycleScope, sfmlInputStream, onParseStorefrontLoadListener)
    ```
2. Alternatively, if the consumer wishes to custom handle threading & exception logic when parsing a storefront:
    ```
    val storefront: StoreFront = HelperManager.getService(SFMLHelper::class.java).parseStorefront(sfmlInputStream)
    ```

## Rendering the SFML document

There are two view components that make up the Storefront view object: `WayfinderView` and `ZoomScrollView`.

The `WayfinderView` view acts as the parent container that accepts only one `ZoomScrollView`.

You may define the `WayfinderView` via XML or programmatically. This view may exist in either an Activity, Fragment or its own custom view.

Example XML implementation:

```
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.wishabi.flipp.skit2demo.ui.storefronts.StorefrontActivity">

    <android.view.WayfinderView
        android:id="@+id/wayfinder"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

The `ZoomScrollView` is generated programmatically.

Once you have a valid `StoreFront` object, you can generate one as such:

```
val storefrontZoomScrollView = StorefrontViewBuilder(this, store).build() as ZoomScrollView
```

To add render the storefront, add the `ZoomScrollView` as a child to the `WayfinderView`:

```
binding.wayfinder.addView(
    storefrontZoomScrollView,
    0,
    FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
)
```

At the end of this step, the view is considered fully rendered.

