package org.view.libwidget

import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Description:
 * @author  admin
 * CreateDate: 2021/5/27
 */

fun FloatingActionButton.hideAndDisable(){
    this.isEnabled = false
    this.hide()
}

fun FloatingActionButton.showAndEnable(){
    this.isEnabled = true
    this.show()
}