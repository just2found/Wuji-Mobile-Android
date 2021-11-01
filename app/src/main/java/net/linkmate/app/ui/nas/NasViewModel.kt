package net.linkmate.app.ui.nas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.reflect.InvocationTargetException

open class NasViewModel(val devId: String) : ViewModel() {

    class ViewModeFactory(private val devId: String) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (NasViewModel::class.java.isAssignableFrom(modelClass)) {
                return try {
                    modelClass.getConstructor(String::class.java).newInstance(devId)
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