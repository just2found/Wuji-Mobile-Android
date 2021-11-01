package net.linkmate.app.poster.utils

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import timber.log.Timber
import java.io.*




object FileUtils {

    fun deleteFiles(file: File) {
        if (file.isFile) {
            file.delete()
            return
        }
        if (file.isDirectory) {
            val childFile = file.listFiles()
            if (childFile == null || childFile.isEmpty()) {
                file.delete()
                return
            }
            for (f in childFile) {
                deleteFiles(f)
            }
            file.delete()
        }
    }

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    fun getFolderSize(file: File) : Long{
        if (!file.exists()){
            return 0
        }
        var size:Long = 0
        try {
            val fileList = file.listFiles()
            for (f in fileList) {
                size += if (f.isDirectory) getFolderSize(f) else f.length()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 删除指定目录下文件及目录
     *
     * @param filePath dir path
     * @return
     */
    fun deleteFolderFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            try {
                if (file.isDirectory) { // 处理目录
                    val files = file.listFiles()
                    for (f in files) {
                        if(f.isFile){
                            f.delete()
                        }
                        else if(f.isDirectory){
                            deleteFolderFile(f.absolutePath)
                        }
                    }
                }
                else if(file.isFile){
                    file.delete()
                }
                file.delete()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取文件的去后缀扩展名
     */
    fun getFileNoExtension2(fileName: String): String {
        try {
            val i = fileName.lastIndexOf(".")
            if (i > 0 && i < fileName.length - 1) {
                return fileName.substring(0, i)//.toLowerCase(Locale.getDefault())
            }
        } catch (e: Exception) {
        }
        return ""
    }

    fun isNotEmpty(str: String?) : Boolean{
        if(str != null && str.isNotEmpty() && str != "null"){
            return true
        }
        return false
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    fun writeToFileFromInput(path: String, fileName: String, input: InputStream): File? {
        var file: File? = null
        var output: OutputStream? = null
        var len: Int
        val buffer = ByteArray(4 * 1024)
        try {
            creatSDDir(path)
            file = creatSDFile(path, fileName)
            output = FileOutputStream(file)
            len = input.read(buffer)
            while (len != -1) {
                output.write(buffer, 0, len)
                len = input.read(buffer)
            }
            output.flush()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            try {
                output?.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return file
    }

    /**
     * 在SD卡上创建文件
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun creatSDFile(path: String, file: String): File? {
        val file = File(path,file)
        file.createNewFile()
        return file
    }

    /**
     * 在SD卡上创建目录
     *
     * @param dir
     */
    fun creatSDDir(dir: String): File? {
        val dir = File(dir)
        if (!dir.exists())
            dir.mkdirs()
        return dir
    }

    /**
     * 判断SD卡上的文件夹是否存在
     */
    fun isFileExist(fileName: String): Boolean {
        val file = File(fileName)
        return file.exists()
    }

    /**
     * GZip解压，tar解包
     *
     * @param srcFile 待压缩的文件或文件夹
     * @param dstDir  压缩至该目录，保持原文件名，后缀改为zip
     */
    fun untarGZip(srcFile: File, dstDir: String) {
        val file = File(dstDir)
        //需要判断该文件存在，且是文件夹
        if (!file.exists() || !file.isDirectory) file.mkdirs()
        val buffer = ByteArray(1024)
        var fis:FileInputStream? = null
        var gcis: GzipCompressorInputStream? = null
        var tais: TarArchiveInputStream? = null
        try {
            fis = FileInputStream(srcFile)
            gcis = GzipCompressorInputStream(fis)
            tais = TarArchiveInputStream(gcis)
            var tarArchiveEntry = tais.nextTarEntry
            while (tarArchiveEntry!= null) {
                val f = File(dstDir + File.separator + tarArchiveEntry.name);
                if (tarArchiveEntry.isDirectory) {
                    f.mkdirs()
                }
                else {
                    val parent = f.parentFile
                    if (!parent.exists()) parent.mkdirs()
                    val fos = FileOutputStream(f)
                    var len = tais.read(buffer)
                    while (len != -1) {
                        fos.write(buffer, 0, len)
                        len = tais.read(buffer)
                    }
                    fos.flush()
                    fos.close()
                }
                tarArchiveEntry = tais.nextTarEntry
            }
        } catch (e:FileNotFoundException) {
            e.printStackTrace();
        } catch (e:IOException) {
            e.printStackTrace();
        } finally {
            try {
                fis?.close()
                //关闭数据流的时候要先关闭外层，否则会报Stream Closed的错误
                tais?.close()
                gcis?.close()
            } catch (e:IOException) {
                e.printStackTrace()
            }
        }
    }

    fun loadFromSDFile(filePath: String) : String? {
        var result: String? = null
        try {
            val f= File(filePath)
            val length = f.length().toInt()
            val buff = ByteArray(length)
            val fin = FileInputStream(f)
            fin.read(buff)
            fin.close()
            result = String(buff)
        }catch (e: Exception){
            e.printStackTrace()
        }
        return result
    }

    fun loadFromFile(file: File) : String? {
        var result: String? = null
        try {
            val length = file.length().toInt()
            val buff = ByteArray(length)
            val fin = FileInputStream(file)
            fin.read(buff)
            fin.close()
            result = String(buff)
        }catch (e: Exception){
            Timber.i("loadFromFile  Exception : ${e.message}")
        }
        return result
    }
}