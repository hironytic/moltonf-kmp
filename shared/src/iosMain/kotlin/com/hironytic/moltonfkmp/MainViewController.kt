package com.hironytic.moltonfkmp

import androidx.compose.ui.window.ComposeUIViewController
import com.hironytic.moltonfkmp.di.initKoin

private val koinStarted: Unit = initKoin()

fun MainViewController() = ComposeUIViewController {
    koinStarted
    App()
}
