package com.hironytic.moltonfkmp.story

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PeriodType {
    @SerialName("prologue") PROLOGUE,
    @SerialName("progress") PROGRESS,
    @SerialName("epilogue") EPILOGUE,
}
