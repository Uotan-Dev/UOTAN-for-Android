package com.gustate.uotan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.databinding.ActivityMainBinding
import com.gustate.uotan.home.HomeFragment
import com.gustate.uotan.notice.NoticeFragment
import com.gustate.uotan.plate.PlateFragment
import com.gustate.uotan.resource.ResFragment
import com.gustate.uotan.user.LoginActivity
import com.gustate.uotan.user.MeFragment
import com.gustate.uotan.utils.CookiesManager
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.isXiaomi
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 创建窗口
        super.onCreate(savedInstanceState)

        // 启用边到边设计，也就是以前的把界面拓展到状态栏和导航栏
        enableEdgeToEdge()

        // 从数据库中获取 Cookies，并判断是否登录
        val cookiesManager = CookiesManager(this)
        cookiesManager.cookiesFlow

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                cookiesManager.cookiesFlow.collect { cookies ->
                    Cookies = cookies
                    isLogin = cookies != mapOf<String,String>()
                    if (!isLogin) {
                        val intent = Intent(baseContext,LoginActivity::class.java)
                        startActivity(intent)
                    }
                    Toast.makeText(baseContext, isLogin.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

        openImmersion(window)

        Toast.makeText(this, isXiaomi().toString(), Toast.LENGTH_SHORT).show()

        /*
         * 修改各个占位布局的高度
         * 以实现小白条与状态栏的沉浸
         */
        // 使用 ViewCompat 类设置一个窗口监听器（这是一个回调函数），需要传入当前页面的根布局
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { _, insets ->
            // systemBars 是一个 insets 对象
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 设置小白条占位布局高度
            binding.gestureView.layoutParams.height = systemBars.bottom
            // 返回 insets
            insets
        }

        binding.mainViewPager.adapter = MainViewPagerAdapter(this)
        binding.mainViewPager.isUserInputEnabled = false

        binding.homeLayout.setOnClickListener { binding.mainViewPager.setCurrentItem(0,true) }
        binding.plateLayout.setOnClickListener { binding.mainViewPager.setCurrentItem(1,true) }
        binding.noticeLayout.setOnClickListener { binding.mainViewPager.setCurrentItem(2,true) }
        binding.resLayout.setOnClickListener { binding.mainViewPager.setCurrentItem(3,true) }
        binding.meLayout.setOnClickListener { binding.mainViewPager.setCurrentItem(4,true) }

        val buttonColor = ContextCompat.getColor(this, R.color.label_tertiary)
        val buttonSelectedColor = ContextCompat.getColor(this, R.color.label_primary)
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                val texts = arrayOf(binding.homeText, binding.plateText, binding.noticeText, binding.resText, binding.meText)

                texts.forEachIndexed { index, textView ->
                    textView.setTextColor(if (index == position) buttonSelectedColor else buttonColor)
                }

                val icons = arrayOf(
                    binding.homeIcon to R.drawable.ic_nav_home,
                    binding.plateIcon to R.drawable.ic_nav_plate,
                    binding.noticeIcon to R.drawable.ic_nav_notice,
                    binding.resIcon to R.drawable.ic_nav_res,
                    binding.meIcon to R.drawable.ic_nav_me
                )

                val selectedIcons = arrayOf(
                    R.drawable.ic_nav_home_selected,
                    R.drawable.ic_nav_plate_selected,
                    R.drawable.ic_nav_notice_selected,
                    R.drawable.ic_nav_res_selected,
                    R.drawable.ic_nav_me_selected
                )

                icons.forEachIndexed { index, (icon, defaultResId) ->
                    icon.setImageDrawable(AppCompatResources
                        .getDrawable(
                            baseContext,
                            if (index == position) selectedIcons[index]
                            else defaultResId
                        )
                    )
                }

            }
        })

    }

}

class MainViewPagerAdapter(fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity){
    private val fragments = listOf(
        HomeFragment(),
        PlateFragment(),
        NoticeFragment(),
        ResFragment(),
        MeFragment()
    )
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}
