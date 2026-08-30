package com.hironytic.moltonfkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hironytic.moltonfkmp.di.initKoin

fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "moltonf-kmp",
        ) {
            App()
        }
    }
}
