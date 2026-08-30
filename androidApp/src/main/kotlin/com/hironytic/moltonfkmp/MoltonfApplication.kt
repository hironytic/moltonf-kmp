package com.hironytic.moltonfkmp

import android.app.Application
import com.hironytic.moltonfkmp.di.initKoin

class MoltonfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(this)
    }
}
