package com.gustate.uotan.fragment.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.activity.SettingsActivity
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.FragmentMeBinding
import com.gustate.uotan.gustatex.view.OptionView
import com.gustate.uotan.utils.parse.user.MeParse
import com.gustate.uotan.utils.Utils
import kotlinx.coroutines.launch

class MeFragment : Fragment() {

    /** 全类变量 **/
    // 初始化视图绑定
    private var _binding: FragmentMeBinding? = null
    // 只能在onCreateView与onDestoryView之间的生命周期里使用
    private val binding: FragmentMeBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 实例化 binding
        _binding = FragmentMeBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 常量设置 **/
        // 设置选项卡
        val settingsOption = view.findViewById<OptionView>(R.id.settingsOption)

        val statusBarView = view.findViewById<View>(R.id.statusBarView)
        val rootView = view.findViewById<View>(R.id.rootView)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.rootScrollView.setPadding(0, systemBars.top + Utils.dp2Px(60, requireContext()).toInt(), 0, systemBars.bottom + Utils.dp2Px(70, requireContext()).toInt())
            // 创建 SimonEdgeIllusion 实例
            TitleAnim(
                binding.title,
                binding.userNameText,
                Utils.dp2Px(60, requireContext()) + systemBars.top.toFloat(),
                systemBars.top.toFloat()
            )
            insets
        }

        // 启动协程
        lifecycleScope.launch {
            val result = MeParse.fetchMeData()
            val avatarImage = view.findViewById<ImageView>(R.id.avatarImage)
            val userNameText = view.findViewById<TextView>(R.id.userNameText)
            val coverImage = view.findViewById<ImageView>(R.id.coverImage)
            val signatureText = view.findViewById<TextView>(R.id.loadingText)
            userNameText.text = result.userName
            signatureText.text = result.signature
            Glide.with(requireContext())
                .load(result.cover)
                .into(coverImage)
            Glide.with(requireContext())
                .load(result.avatar)
                .into(avatarImage)
        }

        /** 设置监听 **/
        // 为设置选项卡设置点击监听
        settingsOption.setOnClickListener {
            // 切换到设置窗口
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

    }

}