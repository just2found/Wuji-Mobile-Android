package org.view.libwidget.anim

import android.view.View
import android.view.animation.AnimationUtils
import org.view.libwidget.R

/** 

Created by admin on 2020/10/17,14:55

 */
fun <T : View> T.shark() {
    val shake = AnimationUtils.loadAnimation(context, R.anim.anim_shark)
    startAnimation(shake)
}