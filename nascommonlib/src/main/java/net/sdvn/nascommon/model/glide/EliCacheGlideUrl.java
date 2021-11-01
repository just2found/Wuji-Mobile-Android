package net.sdvn.nascommon.model.glide;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import io.weline.libimageloader.CacheKeyGlideUrl;

/**
 *  
 * <p>
 * Created by admin on 2020/9/16,11:36
 */
public class EliCacheGlideUrl extends CacheKeyGlideUrl {

    private String mUrl;

    public EliCacheGlideUrl(@NonNull String url) {
        super(url);
//        Timber.d(url);
        mUrl = url;
    }

    @Nullable
    @Override
    public String genCacheKey() {
        return GlideCacheConfig.getImageName(mUrl);
    }
}
