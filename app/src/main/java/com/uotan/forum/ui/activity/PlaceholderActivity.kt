package com.uotan.forum.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.uotan.forum.BaseActivity
import com.uotan.forum.R
import com.uotan.forum.utils.Utils.openImmersion

class PlaceholderActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        openImmersion(window)
        setContentView(R.layout.activity_placeholder)
    }
}