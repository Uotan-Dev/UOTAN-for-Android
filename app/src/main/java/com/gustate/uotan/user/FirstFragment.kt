package com.gustate.uotan.user

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gustate.uotan.R
import com.gustate.uotan.anim.TransitionAnimConfig

class FirstFragment : Fragment() {

    interface FragmentSwitchListener {
        fun switchToFragment(
            targetFragment: Fragment,
            addToBackStack: Boolean = true,
            animConfig: TransitionAnimConfig? = TransitionAnimConfig() // 默认使用右进左出动画
        )
    }

    private var switchListener: FragmentSwitchListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 绑定 Activity 实现的接口
        switchListener = context as? FragmentSwitchListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rootView = view.findViewById<View>(R.id.main)
        val statusBarView = view.findViewById<View>(R.id.statusBarView)
        val gestureView = view.findViewById<View>(R.id.gestureView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            gestureView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.bottom }
            insets
        }
        val login = view.findViewById<View>(R.id.loginCard)
        login.setOnClickListener { switchListener?.switchToFragment(LoginFragment()) }
    }

    override fun onDetach() {
        switchListener = null // 防止内存泄漏
        super.onDetach()
    }

}