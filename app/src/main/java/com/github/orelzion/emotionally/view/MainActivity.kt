package com.github.orelzion.emotionally.view

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.orelzion.emotionally.R
import com.github.orelzion.emotionally.model.network.Repository
import com.github.orelzion.emotionally.model.network.faceDetectApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val byteArray = resources.openRawResource(R.raw.family2_lady1).readBytes()
        try {
            val file = File(filesDir.path + "/stam.jpg").apply {
                createNewFile()
                with(FileInputStream(this)) {
                    writeBytes(byteArray)
                    close()
                }
            }

            CoroutineScope(MainScope().coroutineContext).launch {
                Repository(faceDetectApi)
                    .detectFace(file)
            }
        } catch (exception: Exception) {
            Log.e("dsd", "ds", exception)
        }
    }
}
