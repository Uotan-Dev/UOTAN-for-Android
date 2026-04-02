package com.uotan.forum.welcome.registration

import android.content.Context
import com.uotan.forum.utils.network.HttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException

class RegistrationParse(context: Context) {

    // 创建OkHttpClient并配置CookieJar
    private val client = HttpClient.getClient()

    // 存储动态字段名
    private var dynamicFields = mutableMapOf<String, String>()

    // 获取注册页面并解析动态字段
    fun fetchRegistrationPage(callback: (Result<Boolean>) -> Unit) {
        val request = Request.Builder()
            .url("https://www.uotan.cn/register/")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val html = response.body.string()
                    parseDynamicFields(html)
                    callback(Result.success(true))
                } else {
                    callback(Result.failure(IOException(
                        "获取注册页面失败: ${response.code}")))
                }
            }
        })
    }

    // 解析HTML获取动态字段名
    private fun parseDynamicFields(html: String) {
        val document = Jsoup.parse(html)

        // 查找用户名字段
        val usernameInput = document.select("input[autocomplete=username]").first()
        if (usernameInput != null) {
            dynamicFields["username"] = usernameInput.attr("name")
        }

        // 查找邮箱字段
        val emailInput = document.select("input[autocomplete=email]").first()
        if (emailInput != null) {
            dynamicFields["email"] = emailInput.attr("name")
        }

        // 查找密码字段
        val passwordInput = document.select("input[autocomplete=new-password]").first()
        if (passwordInput != null) {
            dynamicFields["password"] = passwordInput.attr("name")
        }

        // 查找CSRF token
        val csrfTokenInput = document.select("input[name=_xfToken]").first()
        if (csrfTokenInput != null) {
            dynamicFields["_xfToken"] = csrfTokenInput.attr("value")
        }

        // 查找其他可能需要的字段
        val regKeyInput = document.select("input[name=reg_key]").first()
        if (regKeyInput != null) {
            dynamicFields["reg_key"] = regKeyInput.attr("value")
        }

        // 查找时区字段
        val timezoneInput = document.select("input[data-xf-init=auto-timezone]").first()
        if (timezoneInput != null) {
            dynamicFields["timezone"] = timezoneInput.attr("name")
        }
    }

    // 执行注册功能
    fun registerForumAccount(
        username: String,
        email: String,
        password: String,
        callback: (Result<String>) -> Unit
    ) {
        // 确保已获取动态字段
        if (dynamicFields.isEmpty()) {
            callback(Result.failure(IllegalStateException(
                "请先调用fetchRegistrationPage获取动态字段")))
            return
        }

        // 构建多部分表单数据
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("_xfToken", dynamicFields["_xfToken"] ?: "")
            .addFormDataPart(dynamicFields["username"] ?: "username", username)
            .addFormDataPart("username", "")  // 用户名留空字段
            .addFormDataPart(dynamicFields["email"] ?: "email", email)
            .addFormDataPart(dynamicFields["password"] ?: "password", password)
            .addFormDataPart("accept", "1")
            .addFormDataPart("reg_key", dynamicFields["reg_key"] ?: "")
            .addFormDataPart(dynamicFields["timezone"] ?: "timezone",
                "Asia/Hong_Kong")
            .addFormDataPart("_xfRequestUri", "/register/")
            .addFormDataPart("_xfWithData", "1")
            .addFormDataPart("_xfResponseType", "json")
            .build()

        // 创建请求对象
        val request = Request.Builder()
            .url("https://www.uotan.cn/register/register")
            .post(formBody)
            .build()

        // 异步执行请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body.string()
                    callback(Result.success(responseBody))
                } else {
                    callback(Result.failure(IOException(
                        "注册失败: ${response.code}")))
                }
            }
        })
    }
}