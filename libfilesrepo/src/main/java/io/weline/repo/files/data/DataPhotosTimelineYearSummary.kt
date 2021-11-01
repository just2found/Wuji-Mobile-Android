package io.weline.repo.files.data

import com.google.gson.annotations.SerializedName


/**
 *
 * @Description: 获取照片时间线汇总  year data
 * @Author: todo2088
 * @CreateDate: 2021/2/2 15:11
 */
data class DataPhotosTimelineYearSummary(
        @field:SerializedName("count")
        val count: Int,
        @field:SerializedName("file")
        val osFile: OneOSFile,
        @field:SerializedName("year")
        val year: Int
)

