package net.linkmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import net.sdvn.common.repo.NetsRepo

/**
 *  
 *
 *
 * Created by admin on 2020/10/21,10:37
 */
class NetworkViewModel :ViewModel(){
    val networkModels = NetsRepo.getData()
}