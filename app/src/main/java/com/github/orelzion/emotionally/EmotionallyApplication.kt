package com.github.orelzion.emotionally

import android.app.Application

class EmotionallyApplication: Application() {
    companion object {
        lateinit var INSTANCE: EmotionallyApplication
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this
    }
}