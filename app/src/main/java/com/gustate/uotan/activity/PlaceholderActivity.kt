package com.gustate.uotan.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.gustate.uotan.R
import com.gustate.uotan.utils.Utils.Companion.openImmersion

class PlaceholderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openImmersion(window)
        setContentView(R.layout.activity_placeholder)
    }
}