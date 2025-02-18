package com.wishabi.flipp

import android.app.Application
import com.flipp.injectablehelper.HelperManager
import com.flipp.sfml.helpers.ImageLoader
import com.wishabi.flipp.skit2demo.ui.GlideLoader

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HelperManager.getService(ImageLoader::class.java).setImageLoader(GlideLoader(this))
    }
}