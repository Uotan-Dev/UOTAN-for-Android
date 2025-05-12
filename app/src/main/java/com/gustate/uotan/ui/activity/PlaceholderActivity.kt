package com.gustate.uotan.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.utils.Utils.Companion.openImmersion

class PlaceholderActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openImmersion(window)
        setContentView(R.layout.activity_placeholder)
    }
}