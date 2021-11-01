package net.linkmate.app.ui.simplestyle.device.remove_duplicate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import kotlinx.android.synthetic.main.dialog_rd_intelligence.*
import net.linkmate.app.R


class RdIntelligenceDialog: DialogFragment() {

    private val viewModel by viewModels<RemoveDuplicateModel>({ requireParentFragment() })

    private val liveData by lazy {viewModel.selectTypeLiveData}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NO_TITLE, R.style.DialogFullScreen); //dialog全屏
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dialog_rd_intelligence, null)
        view.setOnClickListener { dismiss() }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        longer_name_tv.setOnClickListener {
            liveData.postValue(RemoveDuplicateModel.LONGER_NAME_TYPE)
            dismiss()
        }
        shorter_name_tv.setOnClickListener {
            liveData.postValue(RemoveDuplicateModel.SHORTER_NAME_TYPE)
            dismiss()
        }
        longer_path_tv.setOnClickListener {
            liveData.postValue(RemoveDuplicateModel.LONGER_PATH_TYPE)
            dismiss()
        }
        shorter_path_tv.setOnClickListener {
            liveData.postValue(RemoveDuplicateModel.SHORTER_PATH_TYPE)
            dismiss()
        }
        earlier_time_tv.setOnClickListener {
            liveData.postValue(RemoveDuplicateModel.EARLIER_TIME_TYPE)
            dismiss()
        }
        later_time_tv.setOnClickListener {
            liveData.postValue(RemoveDuplicateModel.LATER_TIME_TYPE)
            dismiss()
        }

        context?.let {
            val textColor = ContextCompat.getColor(it, R.color.color_0C81FB)
            when (liveData.value) {
                RemoveDuplicateModel.LONGER_NAME_TYPE -> {
                    longer_name_tv.setTextColor(textColor)
                }
                RemoveDuplicateModel.SHORTER_NAME_TYPE -> {
                    shorter_name_tv.setTextColor(textColor)
                }
                RemoveDuplicateModel.LONGER_PATH_TYPE -> {
                    longer_path_tv.setTextColor(textColor)
                }
                RemoveDuplicateModel.SHORTER_PATH_TYPE -> {
                    shorter_path_tv.setTextColor(textColor)
                }
                RemoveDuplicateModel.EARLIER_TIME_TYPE -> {
                    earlier_time_tv.setTextColor(textColor)
                }
                RemoveDuplicateModel.LATER_TIME_TYPE -> {
                    later_time_tv.setTextColor(textColor)
                }
                else -> {
                    longer_name_tv.setTextColor(textColor)
                }
            }
        }
    }


}