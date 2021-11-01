package net.linkmate.app.ui.nas.cloud

import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavArgs
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.jvm.JvmStatic
import net.sdvn.nascommon.model.FileManageAction



data class SelectToPathFragmentArgs(
  val deviceid: String,
  val rootPathType: Int,
  val action: FileManageAction = FileManageAction.UPLOAD
  ,val requestKey :String
) : NavArgs {
  @Suppress("CAST_NEVER_SUCCEEDS")
  fun toBundle(): Bundle {
    val result = Bundle()
    result.putString("deviceid", this.deviceid)
    result.putString("requestKey", this.requestKey)
    result.putInt("rootPathType", this.rootPathType)
    if (Parcelable::class.java.isAssignableFrom(FileManageAction::class.java)) {
      result.putParcelable("action", this.action as Parcelable)
    } else if (Serializable::class.java.isAssignableFrom(FileManageAction::class.java)) {
      result.putSerializable("action", this.action as Serializable)
    }
    return result
  }

  companion object {
    @JvmStatic
    fun fromBundle(bundle: Bundle): SelectToPathFragmentArgs {
      bundle.setClassLoader(SelectToPathFragmentArgs::class.java.classLoader)
      val __deviceid : String?
      if (bundle.containsKey("deviceid")) {
        __deviceid = bundle.getString("deviceid")
        if (__deviceid == null) {
          throw IllegalArgumentException("Argument \"deviceid\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"deviceid\" is missing and does not have an android:defaultValue")
      }
      val __requestKey : String?
      if (bundle.containsKey("requestKey")) {
        __requestKey = bundle.getString("requestKey")
        if (__requestKey == null) {
          throw IllegalArgumentException("Argument \"requestKey\" is marked as non-null but was passed a null value.")
        }
      } else {
        throw IllegalArgumentException("Required argument \"requestKey\" is missing and does not have an android:defaultValue")
      }
      val __rootPathType : Int
      if (bundle.containsKey("rootPathType")) {
        __rootPathType = bundle.getInt("rootPathType")
      } else {
        throw IllegalArgumentException("Required argument \"rootPathType\" is missing and does not have an android:defaultValue")
      }
      val __action : FileManageAction?
      if (bundle.containsKey("action")) {
        if (Parcelable::class.java.isAssignableFrom(FileManageAction::class.java) ||
            Serializable::class.java.isAssignableFrom(FileManageAction::class.java)) {
          __action = bundle.get("action") as FileManageAction?
        } else {
          throw UnsupportedOperationException(FileManageAction::class.java.name +
              " must implement Parcelable or Serializable or must be an Enum.")
        }
        if (__action == null) {
          throw IllegalArgumentException("Argument \"action\" is marked as non-null but was passed a null value.")
        }
      } else {
        __action = FileManageAction.UPLOAD
      }
      return SelectToPathFragmentArgs(__deviceid, __rootPathType, __action,requestKey = __requestKey)
    }
  }
}
