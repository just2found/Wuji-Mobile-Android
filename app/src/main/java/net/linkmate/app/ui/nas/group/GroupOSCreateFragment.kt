package net.linkmate.app.ui.nas.group

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.createGraph
import androidx.navigation.fragment.findNavController
import io.weline.repo.api.RETURN_PARAMETER_ERROR
import io.weline.repo.api.V5HttpErrorNo
import kotlinx.android.synthetic.main.fragment_group_o_s_create.*
import libs.source.common.livedata.Status
import libs.source.common.utils.JsonUtilN
import net.linkmate.app.R
import net.linkmate.app.ui.nas.NavTipsBackPressedFragment
import net.linkmate.app.ui.nas.group.adapter.GroupAddMemberAdapter
import net.linkmate.app.ui.nas.group.data.CreateGroupResult
import net.linkmate.app.util.ToastUtils
import net.sdvn.nascommon.fileserver.constants.SharePathType


/**
 * A simple [Fragment] subclass.
 * Use the [GroupOSCreateFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupOSCreateFragment : NavTipsBackPressedFragment() {


    private val viewModel by viewModels<GroupSpaceModel>({ requireParentFragment() })

    override fun getLayoutResId(): Int {
        return R.layout.fragment_group_o_s_create
    }

    override fun getTopView(): View? {
        return title_layout
    }

    override fun initView(view: View) {
        iv_close.setOnClickListener {
            findNavController().popBackStack()
        }

        determine_btn.setOnClickListener {
            val groupName = group_name_it.checkValue
            if (groupName == null) {
                ToastUtils.showToast(R.string.group_name_describe)
                return@setOnClickListener
            }
            devId?.let { it1 ->
                viewModel.createGroupSpace(it1, groupName).observe(this, Observer {
                    if (it.status == Status.SUCCESS) {
                        val createGroupResult: CreateGroupResult? =
                            JsonUtilN.anyToJsonObject(it.data)
                        if (createGroupResult?.id != null) {

                            viewModel.getGroupListJoined(it1)



                            ToastUtils.showToast(R.string.create_success)
                            val toBundle = GroupOSAddMemberFragmentArgs(
                                devId!!,
                                createGroupResult.id,
                                GroupOSAddMemberFragment.ACTION_ADD or GroupOSAddMemberFragment.ACTION_FROM_CREATE
                            ).toBundle()
                            parentFragmentManager.beginTransaction()
                                .replace(
                                    (requireView().parent as View).id,
                                    GroupOSAddMemberFragment::class.java,
                                    toBundle
                                )
                                .commit()
                        } else {
                            ToastUtils.showToast(V5HttpErrorNo.getResourcesId(RETURN_PARAMETER_ERROR))
                        }
                    } else if (it.status == Status.ERROR) {
                        ToastUtils.showToast(V5HttpErrorNo.getResourcesId(it.code))
                    }
                })
            }
        }
    }

    override fun titleColor(): Int {
        return resources.getColor(R.color.white)
    }
}