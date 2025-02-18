package com.wishabi.flipp.com.wishabi.flipp.skit2demo.ui.kotlin

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flipp.injectablehelper.HelperManager
import com.flipp.sfml.ItemAttributes
import com.flipp.sfml.SFArea
import com.flipp.sfml.StoreFront
import com.flipp.sfml.Wayfinder
import com.flipp.sfml.helpers.SFMLHelper
import com.flipp.sfml.helpers.StorefrontAnalyticsManager
import com.flipp.sfml.helpers.StorefrontViewBuilder
import com.flipp.sfml.net.ParseStorefrontHelper
import com.flipp.sfml.views.StorefrontImageView
import com.flipp.sfml.views.StorefrontItemAtomViewHolder
import com.flipp.sfml.views.ZoomScrollView
import com.wishabi.flipp.skit2demo.databinding.ActivityStorefrontKotlinBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL
import java.util.zip.GZIPInputStream

class StorefrontKotlinActivity : AppCompatActivity(),
ParseStorefrontHelper.ParseStorefrontLoadListener {

    companion object {
        val TAG: String? = StorefrontKotlinActivity::class.simpleName

        // Source URL for fetching the SFML document
        const val sfmlUrlSource =
            "https://sfml.flippback.com/680484/3973518/0f27447850c96d16185d2c27d5c8c4bc6757a2e3acfa21fabc9c964748ce4839.sfml"
    }

    private lateinit var binding: ActivityStorefrontKotlinBinding

    // Storefront View to be added as a child view inside of WayfinderView
    private var storefrontZoomScrollView: ZoomScrollView? = null

    // Storefront analytics delegate module
    private var analyticsManager: StorefrontAnalyticsManager = StorefrontAnalyticsManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorefrontKotlinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Optional, set callback events when storefront analytics events happen
        analyticsManager.setAnalyticsEventListener(object : StorefrontAnalyticsManager.AnalyticsEventsListener {
            override fun onWayfinderCategoriesVisibilityChange(visibility: Boolean, category: Wayfinder.WayfinderCategory?) {
                Log.d(TAG, "onWayfinderCategoriesVisibilityChange, $visibility, $category")
            }

            override fun onWayfinderCategorySelected(category: Wayfinder.WayfinderCategory?) {
                Log.d(TAG, "onWayfinderCategorySelected, $category")
            }

            override fun onEngagedVisit() {
                Log.d(TAG, "onEngagedVisit")
            }

            override fun onItemImpression(itemAttributes: MutableList<ItemAttributes>?) {
                Log.d(TAG, "onItemImpression, $itemAttributes")
            }

            override fun onStorefrontOpen() {
                Log.d(TAG, "onStorefrontOpen")
            }
        })

        /**
         * OPTION - 1
         * SDK Recommended method to Fetch & Parse the SFML from the given url using ParseStorefrontHelper
         *
         * Consuming Kotlin app should pass in a lifecycleScope to help SDK handle
         * background operation to fetch the StoreFront Object.
         *
         * ParseStorefrontHelper uses a callback interface to provide response
         *  - onStorefrontParsed
         *  - onStorefrontParseError
         */
        val parseStorefrontHelper = ParseStorefrontHelper()
        parseStorefrontHelper.fetchAndParseStorefront(lifecycleScope, sfmlUrlSource, this)

        /**
         * OPTION - 2
         *  Alternatively, consumers have an option to fetch the sfml document from a given url using the
         *  library of their choice. Use SDK (v2.1.6) provided ParseStorefrontHelper.parseStorefront()
         *  method to parse the input stream into a Storefront object
         *
         *  ParseStorefrontHelper uses a callback interface to provide response
         *   - onStorefrontParsed
         *   - onStorefrontParseError
         *
         */
//        fetchAndParseSfmlUsingCustomLibrary(sfmlUrlSource, this)

        /**
         *  OPTION - 3
         *  Second alternative option is to fetch & parse the sfml while handling the necessary exception & threading logic in both cases.
         *
         *  FETCH - Consuming app has an option to fetch the sfml document from a given url using the
         *          client library of their choice.
         *          - Will need to handle the necessary exception & threading logic.
         *  PARSE - The fetched inputStream is passed to SDK's SFMLHelper.parseStorefront() to
         *          parse into a Storefront object.
         *          - SDK provides a HelperManager as a DI framework to inject SFMLHelper
         *          - Will need to handle the necessary exception & threading logic in both cases
         *
         *    * Successful parsing of the SFML gives a Storefront object which is used for rendering.
         *    * Sample app uses a onStorefrontSuccess() to handle the success logic
         *
         *    * Failure in parsing the sfml can be handled by catching the Exception thrown.
         *    * Sample app uses a onStorefrontError() to handle the exception
         *
         */
//        fetchAndParseSfmlUsingExecuteAsyncTask(sfmlUrlSource)
    }

    /**
     * Callbacks for ParseStorefrontHelper.ParseStorefrontLoadListener
     *
     * Used by OPTION 1 & 2
     */
    override fun onStorefrontParseError(e: java.lang.Exception?) {
        Log.e(TAG, "onStorefrontLoadError: $e")
        Toast.makeText(this, "Failed to render StoreFront: $e", Toast.LENGTH_LONG).show()
    }

    override fun onStorefrontParsed(store: StoreFront) {
        // StoreFront object contains helpers for extracting metadata from the SFML document.
        val storefrontTitle = store.title
        val storefrontSubtitle = store.subtitle
        Log.d(TAG, "onStorefrontSuccess, title:$storefrontTitle, subtitle:$storefrontSubtitle")

        // Build the storefront view and its delegates as a ZoomScollView
        // Item click callbacks are optional if you don't intend to implement click behaviours.
        storefrontZoomScrollView = StorefrontViewBuilder(this, store)
            .setAreaClickListener(object : StorefrontImageView.OnAreaClickListener {
                override fun onAreaClicked(view: View?, area: SFArea?) {
                    Log.d(TAG, "setAreaClickListener#onAreaClicked, $view, $area")
                }

                override fun onAreaLongPressed(view: View?, area: SFArea?) {
                    Log.d(TAG, "setAreaClickListener#onAreaLongPressed, $view, $area")
                }
            })
            .setClipStateDelegate(object : StorefrontImageView.ClipStateDelegate {
                override fun isClipped(itemAttributes: ItemAttributes?): Boolean {
                    Log.d(TAG, "setClipStateDelegate#isClipped, $itemAttributes")
                    return false
                }
            })
            .setMatchupDelegate(object : StorefrontImageView.MatchupDelegate {
                override fun hasMatchup(itemAttributes: ItemAttributes?): Boolean {
                    Log.d(TAG, "setMatchupDelegate#hasMatchup, $itemAttributes")
                    return false
                }

                override fun overrideMatchupIcon(itemAttributes: ItemAttributes?): Drawable {
                    Log.d(TAG, "setMatchupDelegate#overrideMatchupIcon, $itemAttributes")
                    return null!!
                }
            })
            .setItemAtomClickListener(object : StorefrontItemAtomViewHolder.ItemAtomClickListener {
                override fun onItemAtomClick(vh: StorefrontItemAtomViewHolder?) {
                    Log.d(TAG, "setItemAtomClickListener#onItemAtomClick, $vh")
                }

                override fun onItemAtomLongClick(vh: StorefrontItemAtomViewHolder?): Boolean {
                    Log.d(TAG, "setItemAtomClickListener#onItemAtomLongClick, $vh")
                    return false
                }
            })
            .build() as ZoomScrollView

        // Hooking up the analytics module to our newly build Storefront zoom scroll view
        analyticsManager.setStorefrontView(storefrontZoomScrollView)

        // Hooking up the analytics module to our Wayfinder
        analyticsManager.setWayfinderView(binding.wayfinder)

        // Initializing the wayfinder with categories supplied via the SFML document
        binding.wayfinder.setWayfinderDelegates(store.head?.wayfinder?.categories)

        // Inject the Storefront zoom scroll view into the WayfinderView to render the Storefront.
        binding.wayfinder.addView(
            storefrontZoomScrollView,
            0,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    /**
     * OPTION - 2
     * Asynchronously fetches the SFML document from a web url source.
     *
     * In this demo, we use a simple URLConnection, but can be replaced by any other client/library.
     */
    private fun fetchAndParseSfmlUsingCustomLibrary(
        sfmlUrlSource: String,
        onParseStorefrontLoadListener: ParseStorefrontHelper.ParseStorefrontLoadListener
    ) {
        val parseStorefrontHelper = ParseStorefrontHelper()

        lifecycleScope.launch(Dispatchers.IO) {
            // Fetching the SFML document from web
            var sfmlInputStream: InputStream? = null
            try {
                val url = URL(sfmlUrlSource)
                val connection = url.openConnection()
                connection.setRequestProperty("Accept-Encoding", "gzip")
                sfmlInputStream = if ("gzip" == connection.contentEncoding) {
                    GZIPInputStream(connection.getInputStream())
                } else {
                    connection.getInputStream()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Parsing the sfml document into a Storefront object using the SDK Helper
            sfmlInputStream?.let {
                parseStorefrontHelper.parseStorefront(lifecycleScope, sfmlInputStream, onParseStorefrontLoadListener)
            }
        }
    }


    /**
     * OPTION - 3
     * Workaround Main.Looper() issue to allow for coroutines.
     *
     * This method maps AsyncTask to Coroutines
     *
     * Context:
     * - Previously, StorefrontJavaActivity used ParseStorefrontTask(which runs background operations using
     *   AsyncTasks) to parse storefronts. ParseStorefrontTask was deprecated as of SFML v2.1.6
     * - This option is a Kotlin Coroutine alternative to fetching & parsing storefronts using Coroutines
     */
    private fun <R> CoroutineScope.executeAsyncTaskOption3(
        onPreExecute: () -> Unit,
        doInBackground: () -> R,
        onPostExecute: (R) -> Unit
    ) = launch {
        onPreExecute()
        val result = withContext(Dispatchers.Default) {
            doInBackground()
        }
        onPostExecute(result)
    }

    /**
     * OPTION - 3
     * Asynchronously fetches the SFML document and parses the SFML into a `StoreFront` object.
     *
     * In this sample app, we use a simple URLConnection, but can be replaced by any other client/library.
     */
    private fun fetchAndParseSfmlUsingExecuteAsyncTask(sfmlUrlSource: String) {
        lifecycleScope.executeAsyncTaskOption3(
            onPreExecute = {
                // no-op
            },
            doInBackground = {
                // Requesting the SFML document from web
                var sfmlInputStream: InputStream? = null
                try {
                    val url = URL(sfmlUrlSource)
                    val connection = url.openConnection()
                    connection.setRequestProperty("Accept-Encoding", "gzip")
                    sfmlInputStream = if ("gzip" == connection.contentEncoding) {
                        GZIPInputStream(connection.getInputStream())
                    } else {
                        connection.getInputStream()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Parsing the sfml document into a Storefront object
                try {
                    val storefront = HelperManager.getService(SFMLHelper::class.java).parseStorefront(sfmlInputStream)
                    storefront
                } catch (e: Exception) {
                    onStorefrontError(e)
                    null
                } finally {
                    sfmlInputStream?.close()
                }
            },
            onPostExecute = {
                if (it != null) {
                    onStorefrontSuccess(it)
                } else {
                    onStorefrontError(Exception("Parsed storefront was null"))
                }
            })
    }

    /**
     * OPTION - 3
     * Renders the StoreFront object after the SFML document has been fetched and parsed.
     */
    private fun onStorefrontSuccess(store: StoreFront?) {
        // StoreFront object contains helpers for extracting metadata from the SFML document.
        val storefrontTitle = store?.title
        val storefrontSubtitle = store?.subtitle
        Log.d(TAG, "onStorefrontSuccess, title:$storefrontTitle, subtitle:$storefrontSubtitle")

        // Build the storefront view and its delegates as a ZoomScollView
        // Item click callbacks are optional if you don't intend to implement click behaviours.
        storefrontZoomScrollView = StorefrontViewBuilder(this, store)
            .setAreaClickListener(object : StorefrontImageView.OnAreaClickListener {
                override fun onAreaClicked(view: View?, area: SFArea?) {
                    Log.d(TAG, "setAreaClickListener#onAreaClicked, $view, $area")
                }

                override fun onAreaLongPressed(view: View?, area: SFArea?) {
                    Log.d(TAG, "setAreaClickListener#onAreaLongPressed, $view, $area")
                }
            })
            .setClipStateDelegate(object : StorefrontImageView.ClipStateDelegate {
                override fun isClipped(itemAttributes: ItemAttributes?): Boolean {
                    Log.d(TAG, "setClipStateDelegate#isClipped, $itemAttributes")
                    return false
                }
            })
            .setMatchupDelegate(object : StorefrontImageView.MatchupDelegate {
                override fun hasMatchup(itemAttributes: ItemAttributes?): Boolean {
                    Log.d(TAG, "setMatchupDelegate#hasMatchup, $itemAttributes")
                    return false
                }

                override fun overrideMatchupIcon(itemAttributes: ItemAttributes?): Drawable {
                    Log.d(TAG, "setMatchupDelegate#overrideMatchupIcon, $itemAttributes")
                    return null!!
                }
            })
            .setItemAtomClickListener(object : StorefrontItemAtomViewHolder.ItemAtomClickListener {
                override fun onItemAtomClick(vh: StorefrontItemAtomViewHolder?) {
                    Log.d(TAG, "setItemAtomClickListener#onItemAtomClick, $vh")
                }

                override fun onItemAtomLongClick(vh: StorefrontItemAtomViewHolder?): Boolean {
                    Log.d(TAG, "setItemAtomClickListener#onItemAtomLongClick, $vh")
                    return false
                }
            })
            .build() as ZoomScrollView

        // Hooking up the analytics module to our newly build Storefront zoom scroll view
        analyticsManager.setStorefrontView(storefrontZoomScrollView)

        // Hooking up the analytics module to our Wayfinder
        analyticsManager.setWayfinderView(binding.wayfinder)

        // Initializing the wayfinder with categories supplied via the SFML document
        binding.wayfinder.setWayfinderDelegates(store?.head?.wayfinder?.categories)

        // Inject the Storefront zoom scroll view into the WayfinderView to render the Storefront.
        binding.wayfinder.addView(
            storefrontZoomScrollView,
            0,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    /**
     * OPTION - 3
     * Called when there is an exception fetching & parsing the SFML document.
     */
    private fun onStorefrontError(e: Exception) {
        Log.e(TAG, "ERROR: $e")
        Toast.makeText(this, "Failed to render StoreFront: $e", Toast.LENGTH_LONG).show()
    }

}
