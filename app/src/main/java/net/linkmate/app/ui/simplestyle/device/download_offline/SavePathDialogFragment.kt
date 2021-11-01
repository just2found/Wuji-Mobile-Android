package net.linkmate.app.ui.simplestyle.device.download_offline

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_save_path_dialog.*
import net.linkmate.app.R


class SavePathDialogFragment(private val savePath: String?, val onClickListener: View.OnClickListener) : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_save_path_dialog, container, false)
        rootView.setOnClickListener { dismiss() }
        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savePath?.let {
            save_path_tv.text = it
        }
        save_path_tv.setOnClickListener {
            dismiss()
            onClickListener.onClick(it)
        }
        ok_tv.setOnClickListener { dismiss() }
    }

}