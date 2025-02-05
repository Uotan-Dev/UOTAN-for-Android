package com.gustate.uotan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gustate.uotan.databinding.ActivityMainBinding
import com.gustate.uotan.home.HomeFragment
import org.jsoup.Jsoup
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 创建窗口
        super.onCreate(savedInstanceState)

        // 启用边到边设计，也就是以前的把界面拓展到状态栏和导航栏
        enableEdgeToEdge()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootLayout)

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

        binding.mianViewPager.adapter = MainViewPagerAdapter(this)
        binding.mianViewPager.isUserInputEnabled = false
        val buttonColor = ContextCompat.getColor(this, R.color.label_tertiary)
        val buttonSelectedColor = ContextCompat.getColor(this, R.color.label_primary)
        binding.mianViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
                    icon.setImageDrawable(AppCompatResources.getDrawable(baseContext, if (index == position) selectedIcons[index] else defaultResId))
                }
            }
        })
    }

}

class MainViewPagerAdapter(fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity){
    private val fragments = listOf(
        HomeFragment()
    )
    override fun getItemCount(): Int = fragments.size
    override fun createFragment(position: Int): Fragment = fragments[position]
}

fun main() {
    uotan()
}

fun uotan() {
    // 解析网页, document 返回的就是网页 Document 对象
    val document = Jsoup.parse(URL("https://www.uotan.cn/"),30000)
    val pageNav = document.getElementsByClass("pageNav-page ").first()
    val totalPage = pageNav!!.getElementsByTag("a").first()!!.text().toInt()
    println(totalPage)
}