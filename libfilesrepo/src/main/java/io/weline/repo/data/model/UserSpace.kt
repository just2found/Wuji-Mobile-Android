package io.weline.repo.data.model

import androidx.annotation.Keep

/**用户使用空间实体
 * @author Raleigh.Luo
 * date：20/9/23 20
 * describe：
 */
@Keep
data class UserSpace (var used:Long? = 0,//已使用空间
                      var space:Long? = 0,//总空间
                      var files:Long? = 0//文件数量
)