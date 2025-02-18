package com.wishabi.flipp.skit2demo.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.flipp.sfml.helpers.ImageLoader
import com.flipp.sfml.helpers.ImageLoader.ImageTarget
import java.util.*

/**
 * ImageLoader for storefront images.
 *
 * In our SKIT example app we used Picasso, but in our Flipp app we used Glide.
 *
 * Since Glide is the preferred image loading library, we've ported the Flipp version of the image loader over here.
 *
 * TODO: Revisit the idea of forcing our consumers to always provide an image loader.
 *       It might make more sense to have Glide be our default implementation, but we
 *       give our consumers the ability to override it.
 */
class GlideLoader(private val context: Context) : ImageLoader.Loader {

    private val glideImageTargetMap: HashMap<ImageTarget, Target<*>> = HashMap()

    override fun loadInto(url: String, target: ImageTarget) {
        val glideTarget = object : SimpleTarget<Drawable?>() {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                target.onBitmapFailed()
                glideImageTargetMap.remove(target)
            }

            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                var bm: Bitmap? = null
                if (resource is GifDrawable) {
                    bm = resource.firstFrame
                }
                if (resource is BitmapDrawable) {
                    bm = resource.bitmap
                }
                target.onBitmapLoaded(bm)
                glideImageTargetMap.remove(target)
            }
        }

        glideImageTargetMap[target] = glideTarget
        Glide.with(context).load(url).into(glideTarget)
    }

    override fun cancelTarget(target: ImageTarget?) {
        val glideTarget = glideImageTargetMap[target] ?: return
        Glide.with(context).clear(glideTarget)
    }

}
