package com.wishabi.flipp.skit2demo.ui.java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WayfinderView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.flipp.injectablehelper.HelperManager;
import com.flipp.sfml.ItemAttributes;
import com.flipp.sfml.SFHead;
import com.flipp.sfml.StoreFront;
import com.flipp.sfml.Wayfinder;
import com.flipp.sfml.helpers.ImageLoader;
import com.flipp.sfml.helpers.StorefrontAnalyticsManager;
import com.flipp.sfml.helpers.StorefrontViewBuilder;
import com.flipp.sfml.net.ParseStorefrontHelper;
import com.flipp.sfml.views.StorefrontItemAtomViewHolder;
import com.flipp.sfml.views.ZoomScrollView;
import com.wishabi.flipp.skit2demo.R;
import com.wishabi.flipp.skit2demo.ui.GlideLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class StorefrontJavaActivity extends AppCompatActivity implements
        ParseStorefrontHelper.ParseStorefrontLoadListener,
        StorefrontAnalyticsManager.AnalyticsEventsListener,
        StorefrontItemAtomViewHolder.ItemAtomClickListener {

    String TAG = StorefrontJavaActivity.class.getSimpleName();

    // Source URL for fetching the SFML document
    String sfmlUrlSource = "https://sfml.flippback.com/680484/3973518/0f27447850c96d16185d2c27d5c8c4bc6757a2e3acfa21fabc9c964748ce4839.sfml";

    // Storefront View to be added as a child view inside of WayfinderView
    private ZoomScrollView storefrontZoomScrollView;

    // Storefront analytics delegate module
    private StorefrontAnalyticsManager analyticsManager;

    private WayfinderView mStorefrontWrapper;

    private ArrayList<StorefrontItemAtomViewHolder> mHeroItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storefront_java);

        // Optional, set callback events when storefront analytics events happen
        analyticsManager = new StorefrontAnalyticsManager();
        analyticsManager.setAnalyticsEventListener(this);

        mStorefrontWrapper = findViewById(R.id.java_storefront_wayfinder);
        mHeroItems = new ArrayList<>();

        /**
         * OPTION - 1
         * SDK recommended method to fetch & parse Storefront object from a given sfml source url
         * (Recommended since v2.1.6)
         *
         *  - Parse the SFML from the given url using ParseStorefrontHelper
         *  - SDK handles background operation to fetch & parse the StoreFront Object
         *  - Check following callbacks for response:
         *      * - onStorefrontParsed
         *      * - onStorefrontParseError
         */
        ParseStorefrontHelper parseStorefrontHelper = new ParseStorefrontHelper();
        parseStorefrontHelper.fetchAndParseStorefront(sfmlUrlSource, this);

        /**
         * OPTION - 2
         * Alternatively consumers can use their own library to fetch the sfml document from the network
         *  and use ParseStorefrontHelper from the SDK only to parse sfml into a Storefront object
         *
         *  FETCH - Consuming client app has an option to fetch the sfml document from a given url using the
         *          client library of their choice.
         *        - Will also need to handle the necessary threading & exceptions
         *  PARSE - The fetched inputStream is passed to SDK's ParseStorefrontHelper.parseStorefront() to
         *          parse into a Storefront object.
         *          Check following callbacks for response:
         *           - onStorefrontParsed
         *           - onStorefrontParseError
         */
//         fetchAndParseSfmlUsingOwnClient(sfmlUrlSource, this);
    }

    /**
     * fetchAndParseSfmlUsingOwnClient uses Executor to asynchronously run
     * parseStorefrontExecutorTask in the background
     *
     * @param sfmlUrlSource
     * @param parseStorefrontLoadListener
     */
    private void fetchAndParseSfmlUsingOwnClient(
        String sfmlUrlSource,
        ParseStorefrontHelper.ParseStorefrontLoadListener parseStorefrontLoadListener
    ) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() ->
            parseStorefrontExecutorTask(sfmlUrlSource, parseStorefrontLoadListener)
        );
    }

    /**
     * Executor runs the parseStorefrontExecutor in the background to fetch sfml from a source url
     * and then parse it into a Storefront object
     *
     * @param sfmlUrlSource
     * @param parseStorefrontLoadListener
     */
    private void parseStorefrontExecutorTask(
        String sfmlUrlSource,
        ParseStorefrontHelper.ParseStorefrontLoadListener parseStorefrontLoadListener
    ) {
        InputStream sfmlInputStream;
        ParseStorefrontHelper parseStorefrontHelper = new ParseStorefrontHelper();

        // step 1 - Fetch sfml from a source url
        URL url;
        try {
            url = new URL(sfmlUrlSource);
            URLConnection  connection = url.openConnection();
            connection.setRequestProperty("Accept-Encoding", "gzip");

            if("gzip".equals(connection.getContentEncoding())) {
                sfmlInputStream = new GZIPInputStream(connection.getInputStream());
            } else {
                sfmlInputStream = connection.getInputStream();
            }

            // step 2 - Parse sfml into a Storefront object using the SDK
            if(sfmlInputStream != null) {
                parseStorefrontHelper.parseStorefront(sfmlInputStream, parseStorefrontLoadListener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ParseStorefrontHelper.onStorefrontParseError
     * @param e
     *
     * Triggered when an loading/parsing SFML file throws an Exception
     * Used by OPTION - 1
     */
    @Override
    public void onStorefrontParseError(@Nullable Exception e) {
        Log.e(TAG, "onStorefrontParseError: $e");
        Toast.makeText(this, "onStorefrontParseError: $e", Toast.LENGTH_SHORT).show();
    }

    /**
     * ParseStorefrontHelper.onStorefrontParsed
     * @param store
     *
     * Triggered when an SFML file is successfully loaded and parsed
     * Used by OPTION - 1
     */
    @Override
    public void onStorefrontParsed(@NonNull StoreFront store) {
        HelperManager.getService(ImageLoader.class).setImageLoader(new GlideLoader(this));

        // TODO: update your toolbar with the values from store.getTitle() and store.getSubtitle()

        storefrontZoomScrollView = (ZoomScrollView) new StorefrontViewBuilder(this, store)
                .setAnalyticsManager(analyticsManager)
                .setItemAtomClickListener(this)
                .build();
        analyticsManager.setStorefrontView(storefrontZoomScrollView);
        analyticsManager.setWayfinderView(mStorefrontWrapper);
        mStorefrontWrapper.addView(storefrontZoomScrollView,
                0,
                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // setup wayfinder
        SFHead head = store.getHead();
        if (head == null) {
            return;
        }
        Wayfinder wayfinder = head.getWayfinder();
        if (wayfinder == null) {
            return;
        }
        List<Wayfinder.WayfinderCategory> ways = wayfinder.getCategories();
        mStorefrontWrapper.setWayfinderDelegates(ways);
    }

    @Override
    public void onWayfinderCategoriesVisibilityChange(boolean b, @Nullable Wayfinder.WayfinderCategory wayfinderCategory) {

    }

    @Override
    public void onWayfinderCategorySelected(@Nullable Wayfinder.WayfinderCategory wayfinderCategory) {

    }

    @Override
    public void onEngagedVisit() {

    }

    @Override
    public void onItemImpression(List<ItemAttributes> list) {

    }

    @Override
    public void onStorefrontOpen() {

    }

    @Override
    public void onItemAtomClick(StorefrontItemAtomViewHolder storefrontItemAtomViewHolder) {

    }

    @Override
    public boolean onItemAtomLongClick(StorefrontItemAtomViewHolder storefrontItemAtomViewHolder) {
        return false;
    }
}