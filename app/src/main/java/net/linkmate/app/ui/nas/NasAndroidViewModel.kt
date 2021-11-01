package net.linkmate.app.ui.nas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.InvocationTargetException
/**
 *
 * @Description: 带参数ViewModel
 * @Author: todo2088
 * @CreateDate: 2021/2/2 14:29
 *
 * @constructor 相应的构造方法需要添加  @Keep ,否则混淆之后报错
 */

open class NasAndroidViewModel(app1: Application, val devId: String) : AndroidViewModel(app1) {

    class ViewModeFactory(private val mApplication: Application, private val devId: String) : ViewModelProvider.AndroidViewModelFactory(mApplication) {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (NasAndroidViewModel::class.java.isAssignableFrom(modelClass)) {
                return try {
                    modelClass.getConstructor(Application::class.java, String::class.java).newInstance(mApplication, devId)
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                } catch (e: IllegalAccessException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                } catch (e: InstantiationException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException("Cannot create an instance of $modelClass", e)
                }
            }
            return super.create(modelClass)
        }
    }
}
