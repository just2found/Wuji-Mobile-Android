package net.linkmate.app.poster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.fragment_tab.*
import kotlinx.android.synthetic.main.item_poster_video.view.*
import kotlinx.android.synthetic.main.pop_window.view.*
import net.linkmate.app.R
import net.linkmate.app.base.MyApplication
import net.linkmate.app.poster.model.LeftTabModel
import net.linkmate.app.poster.model.MediaInfoModel
import net.linkmate.app.poster.utils.BitmapUtils
import net.sdvn.nascommon.constant.AppConstants
import net.sdvn.nascommon.model.oneos.OneOSFile
import net.sdvn.nascommon.model.oneos.user.LoginSession
import net.sdvn.nascommon.utils.FileUtils
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TabFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TabFragment(_domain: String,topTabPosterData: ArrayList<MediaInfoModel>?,leftTabs: List<LeftTabModel>,loginSession: LoginSession?, isComeCircle: Boolean) : Fragment() {
//  private var param1: String? = null
  private var domain = _domain
  private var mLoginSession = loginSession
  private var mLeftTabs = leftTabs
  private var mTopTabPosterData = if (mLeftTabs.isNullOrEmpty()) topTabPosterData else null
  private var posterVideoAdapter: BaseQuickAdapter<MediaInfoModel, BaseViewHolder>? = null
  private var recycle_view_poster: RecyclerView? = null
//  private var secondTitles = ArrayList<String>()
  private var currentPosition = -1
  private val isComeCircle = isComeCircle

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    /*arguments?.let {
      param1 = it.getString(ARG_PARAM1)
      Log.i("cyb", "param1=$param1")
    }*/
    /*mLeftTabs.forEach {
      secondTitles.add(it.name)
    }*/
    if(!mLeftTabs.isNullOrEmpty()){
      currentPosition = 0
    }
    posterVideoAdapter = object : BaseQuickAdapter<MediaInfoModel, BaseViewHolder>(R.layout.item_poster_video) {
      override fun convert(helper: BaseViewHolder, item: MediaInfoModel) {
        helper.itemView.videoNameTextView.text = item.title
        /*if(isNotEmpty(item.map[NFO_SET]))
        item.map[NFO_SET]
        else
        item.map[NFO_TITLE]*/
        item.posterList?.let {
          if(it.isNotEmpty()){
            /*val url = "http://${mLoginSession?.deviceInfo?.vIp}:9898/file/download?session=${mLoginSession?.session}&path=${it[0]}"
            Glide.with(MyApplication.getContext())
                    .load(url)
                    .into(helper.itemView.videoImage)*/
            BitmapUtils().loadingAndSaveImg(
                    it[0],
                    helper.itemView.videoImage,
                    "${MyApplication.getContext().filesDir?.path}/images/poster/${mLoginSession?.deviceInfo?.id.toString()}",
                    mLoginSession?.session!!,mLoginSession?.deviceInfo?.vIp!!, mLoginSession?.deviceInfo?.id.toString(),
                    MyApplication.getContext())
          }
        }
      }

      /*override fun onViewRecycled(holder: BaseViewHolder) {
        Glide.with(MyApplication.getContext()).clear(holder.itemView.videoImage)
        super.onViewRecycled(holder)
      }*/
    }
    posterVideoAdapter?.setHasStableIds(true)
    //影片点击
    posterVideoAdapter?.setOnItemClickListener{ _, _, position ->
      val intent = Intent(context, PosterDetailActivity::class.java)
      intent.putExtra("data", posterVideoAdapter?.data?.get(position) as MediaInfoModel)
      intent.putExtra("domain", domain)
      intent.putExtra("ip", mLoginSession?.deviceInfo?.vIp)
      intent.putExtra("id", mLoginSession?.id)
      intent.putExtra("session", mLoginSession?.session)
      intent.putExtra("isComeCircle", isComeCircle)
      startActivity(intent)
//      mLoginSession?.let {
//        val selectedList: ArrayList<OneOSFile> = arrayListOf()
//        val path = posterVideoAdapter?.data?.get(position)?.path
//        val pathS = path?.split("/")
//        val file = OneOSFile()
//        file.share_path_type = 2
//        file.setPath(path)
//        FileUtils.openOneOSFile(
//                it,
//                MyApplication.getContext(),
//                file
//        )
        /*if(pathS != null && pathS.isNotEmpty()){
        val name = pathS[(pathS.size-1)]
        file.setName(name)
      }
      file.setTime(System.currentTimeMillis() / 1000)
      selectedList.add(file)
      onClickDownloadFiles(selectedList)*/
//      }

    }
  }

  override fun onCreateView(
          inflater: LayoutInflater, container: ViewGroup?,
          savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    Log.d("TabFragment", "onCreateView mLeftTabs size = ${mLeftTabs.size}")
    return inflater.inflate(R.layout.fragment_tab, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    Log.d("TabFragment", "onViewCreated mLeftTabs size = ${mLeftTabs.size}")
    recycle_view_poster = view.findViewById(R.id.recycle_view_poster)
//    view.findViewById<TextView>(R.id.tv).text = param1
    recycle_view_poster?.adapter = posterVideoAdapter
    recycle_view_poster?.layoutManager = GridLayoutManager(MyApplication.getContext(), 3)
    setCurrentPosition(currentPosition)
    /*fabMenu.setOnClickListener {
      showPopupWindow(it)
    }*/
  }

  fun getCurrentPosition() : Int{
    return currentPosition
  }

  fun setCurrentPosition(position: Int){
    currentPosition = position
    if (currentPosition != -1) {
//      val data = mLeftTabs[currentPosition].posterData?.filterIndexed { index, _ -> index < 20 }?.toList()
      posterVideoAdapter?.setNewData(mLeftTabs[currentPosition].posterData)
    } else {
//      val data = mLeftTabs[currentPosition].posterData?.filterIndexed { index, _ -> index < 20 }?.toList()
      posterVideoAdapter?.setNewData(mTopTabPosterData)
    }
  }

  /*private fun showPopupWindow(view: View?) {
    val rootView = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.pop_window, null)
    rootView.rvMovieType.adapter = PupRvAdapter(secondTitles)
    rootView.rvMovieType.layoutManager = LinearLayoutManager(MyApplication.getContext())
    val popupWindow = PopupWindow(
            rootView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            600,
            true
    )
    popupWindow.isTouchable = true
    popupWindow.setBackgroundDrawable(resources.getDrawable(R.drawable.popupwindow_bg))
    popupWindow.showAsDropDown(view, 0, -10, Gravity.BOTTOM)
  }*/

  companion object {
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TabFragment.
     */
    // TODO: Rename and change types and number of parameters
    @JvmStatic
    fun newInstance(_domain: String,topTabPosterData: ArrayList<MediaInfoModel>?,leftTabs: List<LeftTabModel>,loginSession: LoginSession?, isComeCircle: Boolean) =
            TabFragment(_domain,topTabPosterData,leftTabs,loginSession,isComeCircle).apply {
              /*arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
              }*/
            }
  }
}