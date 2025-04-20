package com.gustate.uotan

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.ActivityThemeBinding
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlin.math.roundToInt

class ThemeActivity : BaseActivity() {
    private lateinit var binding: ActivityThemeBinding
    private var isUserInput = true
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        openImmersion(window)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.srlRoot.setPadding(
                0,
                (systemBars.top + 60f.dpToPx(this)).roundToInt(),
                0,
                systemBars.bottom
            )
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(
                binding.title,
                binding.bigTitle,
                (systemBars.top + 60f.dpToPx(this)),
                systemBars.top.toFloat()
            )
            insets
        }
        val switchMonet = findViewById<Switch>(R.id.switchMonet)
        when (ThemePreference.getTheme(this)) {
            "base" -> {
                isUserInput = false
                switchMonet.isChecked = false
                isUserInput = true
            }
            "monet" -> {
                isUserInput = false
                switchMonet.isChecked = true
                isUserInput = true
            }
            else -> {
                isUserInput = false
                switchMonet.isChecked = false
                isUserInput = true
            }
        }
        switchMonet.setOnCheckedChangeListener { _, isChecked ->
            if (isUserInput) {
                if (isChecked) {
                    ThemePreference.saveTheme(this, "monet")
                    this.recreate()
                } else {
                    ThemePreference.saveTheme(this, "base")
                    this.recreate()
                }
            }
        }
    }
}