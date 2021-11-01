package net.linkmate.app.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.exoplayer2.Player
import kotlinx.android.synthetic.main.layout_video_popup.view.*
import net.linkmate.app.R
import net.linkmate.app.ui.simplestyle.dynamic.video.PlayerHolder
import net.linkmate.app.ui.simplestyle.dynamic.video.PlayerState

/**
 * @author Raleigh.Luo
 * date：21/1/26 14
 * describe：
 */
class VideoPlay : FrameLayout {

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)

    private val playerState by lazy { PlayerState() }
    private val playerHolder: PlayerHolder

    init {
        this.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_video_popup, this)
        playerHolder = PlayerHolder(context, playerState, mExoplayerview)
    }

    fun addListener(listener: Player.EventListener) {
        playerHolder.addListener(listener)
    }

//    // Android lifecycle hooks.
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        // While the user is in the app, the volume controls should adjust the music volume.
//        volumeControlStream = AudioManager.STREAM_MUSIC
//        createPlayer()
//    }


    //继续播放
    fun continuePlayer() {
        playUri?.let {
            playerHolder.start(it)
        }
    }

    private var playUri: Uri? = null
    fun startPlayer(uri: Uri) {
        playUri = uri
//        val uri = Uri.parse("https://v-cdn.zjol.com.cn/280443.mp4")
        playerHolder.start(uri)
    }

    fun setPlayUri(uri: Uri) {
        playUri = uri
    }

    /**
     * 停止播放
     */
    fun stopPlayer() {
        playerHolder.stop()
    }

    /**
     * 释放资源
     */
    fun releasePlayer() {
        playerHolder.release()
    }
}