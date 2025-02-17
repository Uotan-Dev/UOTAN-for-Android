package com.gustate.uotan.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.R
import com.gustate.uotan.parse.LoginParse
import com.gustate.uotan.utils.CookiesManager
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var cookiesManager: CookiesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        cookiesManager = CookiesManager(requireContext())

        val rootView = view.findViewById<View>(R.id.main)
        val statusBarView = view.findViewById<View>(R.id.statusBarView)
        val gestureView = view.findViewById<View>(R.id.gestureView)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            gestureView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.bottom }
            insets
        }


        val loginButton = view.findViewById<View>(R.id.loginCard)
        val usernameEdit = view.findViewById<EditText>(R.id.username_email)
        val passwordEdit = view.findViewById<EditText>(R.id.password)
        loginButton.setOnClickListener {
            val username = usernameEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            when {
                username.isEmpty() && password.isEmpty() ->
                    Toast.makeText(context, R.string.please_press_username_password, Toast.LENGTH_SHORT).show()
                username.isEmpty() ->
                    Toast.makeText(context, R.string.please_press_username, Toast.LENGTH_SHORT).show()
                password.isEmpty() ->
                    Toast.makeText(context, R.string.please_press_password, Toast.LENGTH_SHORT).show()
                else -> lifecycleScope.launch {
                    try {
                        val cookies = LoginParse.login(username, password)
                        if (cookies != mapOf<String,String>()){
                            cookiesManager.saveCookies(cookies)
                            Toast.makeText(context, "登录成功：${cookies.size}条凭证", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, R.string.password_incorrect, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "登录失败：${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

}