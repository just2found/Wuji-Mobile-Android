package net.linkmate.app.poster.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Looper
import android.text.TextUtils
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class BitmapUtils {
    /**
     * 加载并保存图片
     */
    fun loadingAndSaveImg(
        imagePath: String?, view: ImageView, filesDirPath: String,
        session: String, ip: String, id: String, context: Context,
        errorImgId: Int = -1, loadingImgId: Int = -1,
        isThumbnail: Boolean = true
    )
    {
        if(!isNotEmpty(imagePath)) {
            return
        }
        if(loadingImgId != -1){
            view.setImageResource(loadingImgId)
        }
        else if(errorImgId != -1){
            view.setImageResource(errorImgId)
        }
        val ftPath = "${id}${imagePath}".replace("/", "-")
        val file = File(filesDirPath, ftPath)
        if(file.exists())
        {
            Glide
                .with(context)
                .load(file)
                .skipMemoryCache(true)
                .into(view)
        }
        else
        {
            val url =
                    if (isThumbnail)
                        "http://${ip}:9898/file/thumbnail?session=${session}&size=max&path=${imagePath}&d=${Date().time}"
                    else
                        "http://${ip}:9898/file/download?session=${session}&path=${imagePath}&d=${Date().time}"
                Glide
                    .with(context)
                    .asDrawable()
                    .load(url)
                    .skipMemoryCache(true)
                    .listener(MyRequestListener(ftPath, filesDirPath,view,errorImgId,context))
                    .into(view)
        }
    }

    fun loadingAndSaveImg(
            imagePath: String?, view: ImageView, filesDirPath: String,context: Context,
            errorImgId: Int = -1, loadingImgId: Int = -1
    ) {
        if(!isNotEmpty(imagePath)) {
            return
        }
        if(loadingImgId != -1){
            view.setImageResource(loadingImgId)
        }
        else if(errorImgId != -1){
            view.setImageResource(errorImgId)
        }
        val ftPath = "$imagePath".replace("/", "-")
        val file = File(filesDirPath, ftPath)
        if(file.exists())
        {
            Glide
                    .with(context)
                    .load(file)
                    .skipMemoryCache(true)
                    .into(view)
        }
        else
        {
            Glide
                    .with(context)
                    .asDrawable()
                    .load(imagePath)
                    .skipMemoryCache(true)
                    .listener(MyRequestListener(ftPath, filesDirPath,view,errorImgId,context))
                    .into(view)
        }
    }

    class MyRequestListener(ftPath: String, filesDirPath: String, view: ImageView,
                            errorImgId: Int = -1, context: Context) : RequestListener<Drawable> {

        val ftPath = ftPath
        val filesDirPath = filesDirPath
        val errorImgId = errorImgId
        val view = view
        val context = context

        override fun onLoadFailed(
            e: GlideException?, model: Any?,
            target: Target<Drawable>?, isFirstResource: Boolean
        ): Boolean {
            if(errorImgId != -1){
                view.setImageResource(errorImgId)
            }
            return false
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?,
            dataSource: DataSource?, isFirstResource: Boolean
        ): Boolean {
            if (resource != null) {
                BitmapUtils().saveDrawable(filesDirPath, ftPath, resource)
            }
            return false
        }
    }

    interface DownloadListener{
        fun downloadListener(isSuccess: Boolean)
    }

    /**
     * 下载图片
     */
    fun downloadImg(
        context: Context, imagePath: String, filesDirPath: String,
        session: String, ip: String, id: String, downloadListener: DownloadListener?
    )
    {
        val ftPath = "${id}${imagePath}".replace("/", "-")
        if(File(filesDirPath, ftPath).exists()) {
            downloadListener?.downloadListener(true)
        }
        else {
            val url = "http://${ip}:9898/file/download?session=${session}&path=${imagePath}&d=${Date().time}"
            Glide
                .with(context)
                .asDrawable()
                .load(url)
                .skipMemoryCache(true)
                .into(MySimpleTarget(ftPath,filesDirPath,downloadListener))
        }
    }
    class MySimpleTarget(ftPath: String, filesDirPath: String,downloadListener: DownloadListener?) :SimpleTarget<Drawable>(){
        val ftPath = ftPath
        val filesDirPath = filesDirPath
        val downloadListener: DownloadListener? = downloadListener
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            BitmapUtils().saveDrawable(filesDirPath, ftPath, resource)
            downloadListener?.downloadListener(true)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            downloadListener?.downloadListener(false)
            super.onLoadFailed(errorDrawable)
        }
    }

    class MyLocalRequestListener(ftPath: String, filesDirPath: String,downloadListener: DownloadListener?) : RequestListener<Drawable> {

        val ftPath = ftPath
        val filesDirPath = filesDirPath
        val downloadListener: DownloadListener? = downloadListener

        override fun onLoadFailed(
            e: GlideException?, model: Any?,
            target: Target<Drawable>?, isFirstResource: Boolean
        ): Boolean {
            downloadListener?.downloadListener(false)
            return false
        }

        override fun onResourceReady(
            resource: Drawable?, model: Any?, target: Target<Drawable>?,
            dataSource: DataSource?, isFirstResource: Boolean
        ): Boolean {
            if (resource != null) {
                BitmapUtils().saveDrawable(filesDirPath, ftPath, resource)
                downloadListener?.downloadListener(true)
            }
            else {
                downloadListener?.downloadListener(false)
            }
            return false
        }
    }

    /**
     * 保存图片 Bitmap
     */
    fun saveBitmap(targetPath: String, bitmapPath: String, bm: Bitmap) {
        //指定我们想要存储文件的地址
        //val targetPath: String = mContext.filesDir.toString() + "/images/"
        //Log.d("Save Bitmap", "Save Path=$TargetPath")
        val dir = File(targetPath)
        if (!dir.exists())
            dir.mkdirs()
        val saveFile = File(targetPath, bitmapPath)
        saveFile.createNewFile()
        val saveImgOut = FileOutputStream(saveFile)
        try {
            // compress - 压缩的意思
            if(bitmapPath.endsWith("png", true)){
                bm.compress(Bitmap.CompressFormat.PNG, 100, saveImgOut)
            }
            else{
                bm.compress(Bitmap.CompressFormat.JPEG, 100, saveImgOut)
            }
            //存储完成后需要清除相关的进程
            saveImgOut.flush()
            saveImgOut.close()
            //Log.d("Save Bitmap", "The picture is save to your phone!")
        } catch (ex: IOException) {
            ex.printStackTrace()
            saveImgOut.flush()
            saveImgOut.close()
        }
    }

    /**
     * 保存图片 Drawable
     */
    fun saveDrawable(targetPath: String, bitmapPath: String, drawable: Drawable) {
        var bm = drawableToBitamp(drawable)
        if (bm != null) {
            saveBitmap(targetPath,bitmapPath,bm)
            bm = null
        }
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private fun drawableToBitamp(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 删除图片
     */
    fun deleteBitmap(targetPath: String, bitmapPath: String) {
        val dir = File(targetPath)
        if (!dir.exists())
            return
        val saveFile = File(targetPath, bitmapPath)
        if(saveFile.isFile){
            saveFile.delete()
        }
    }

    /**
     * 清理图片磁盘缓存
     */
    private fun clearImageDiskCache(context: Context){
        try{
            if(Looper.myLooper() == Looper.getMainLooper()){
                Thread{
                    Glide.get(context).clearDiskCache()
                }.start()
            }else{
                Glide.get(context).clearDiskCache();
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 清除图片内存缓存
     */
    private fun clearImageMemoryCache(context: Context){
        try{
            if(Looper.myLooper() == Looper.getMainLooper()){
                Glide.get(context).clearMemory();
            }
        }catch(e: Exception){
            e.printStackTrace()
        }
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath filePath
     * @param deleteThisPath deleteThisPath
     */
    private fun deleteFolderFile(filePath: String, deleteThisPath: Boolean) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) {
                    val files = file.listFiles()
                    for (file1 in files) {
                        deleteFolderFile(file1.absolutePath, true)
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory) {
                        file.delete()
                    } else {
                        if (file.listFiles().isEmpty()) {
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 清除图片所有缓存
     * 主要调用这个方法
     */
    fun clearImageAllCache(context: Context) {
        clearImageDiskCache(context)
        clearImageMemoryCache(context)
        val imageExternalCatchDir ="${context.externalCacheDir}${ExternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR}"
        deleteFolderFile(imageExternalCatchDir, true)
    }

    fun isNotEmpty(str: String?) : Boolean{
        if(str != null && str.isNotEmpty() && str != "null"){
            return true
        }
        return false
    }

    fun roundDrawableByDrawable(
            drawable: Drawable,
            outWidth: Int,
            outHeight: Int,
            radius: Float,
            resources: Resources
    ): Drawable? {
        val bitmap = drawableToBitamp(drawable) ?: return null
        if (bitmap.width == 0 || bitmap.height == 0) return drawable
        // 等比例缩放拉伸
        val widthScale = outWidth * 1.0f / bitmap.width
        val heightScale = outHeight * 1.0f / bitmap.height
        val matrix = Matrix()
        matrix.setScale(widthScale, heightScale)
        val newBt = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        // 绘制圆角
        val dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, newBt)
        dr.cornerRadius = radius
        dr.setAntiAlias(true)
        return dr
    }
}