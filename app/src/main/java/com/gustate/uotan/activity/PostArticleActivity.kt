package com.gustate.uotan.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.loader.content.CursorLoader
import com.gustate.uotan.BaseActivity
import com.gustate.uotan.R
import com.gustate.uotan.databinding.ActivityPostAritcleBinding
import com.gustate.uotan.gustatex.view.SelectableEditText
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.REQUEST_CODE_PERMISSION
import com.gustate.uotan.utils.Utils.Companion.USER_AGENT
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.kongzue.dialogx.dialogs.BottomDialog
import com.kongzue.dialogx.interfaces.OnBindView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.File
import java.io.IOException
import androidx.core.graphics.drawable.toDrawable
import com.gustate.uotan.gustatex.dialog.LoadingDialog
import com.gustate.uotan.gustatex.view.ScrollControllerListView
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.kongzue.dialogx.interfaces.DialogLifecycleCallback
import kotlin.math.roundToInt


/**
 * 发帖页面 (Activity)
 * JiaGuZhuangZhi Miles
 * Gustate 02/24/2025
 * I Love Jiang’Xun
 */
class PostArticleActivity : BaseActivity() {

    /** 全类变量 **/
    /* 延迟启动 */
    // 视图绑定
    private lateinit var binding: ActivityPostAritcleBinding
    // 当前发布页 (WebView) 的 Document 文档
    private lateinit var addFileLink: String
    // 当前发布页 (WebView) 的 xf_token
    private lateinit var xfToken: String
    // 当前发布页 (WebView) 的 cookie
    private lateinit var cookieString: String
    // 版块页
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var sectionName: String
    private lateinit var sectionUrl: String
    companion object {
        private var pitchPrefixName: String = "(无前缀)"
        private var pitchPrefixId: Int = 0
        private var pitchPrefixPosition: Int = 0
        private var prefixNameList = mutableListOf<String>()
        private var prefixIdList = mutableListOf<Int>()
    }
    /* 直接赋值 */
    // 文本操作是否为用户点击
    private var isUserTextAction = true

    /** 全类常量 **/
    // 创建一个用于储存用户选择图片的 Map
    private val pictureMap = mutableMapOf<String, String>()

    /**
     * 当页面被创建时
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        openImmersion(window)

        val loadingDialog = LoadingDialog(this)

        binding = ActivityPostAritcleBinding.inflate(layoutInflater)
        setContentView(binding.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.layoutParams.height = systemBars.top
            binding.statusBarBlurView.layoutParams.height = systemBars.top
            val appBarLayoutMargin = binding.appBarLayout.layoutParams as MarginLayoutParams
            appBarLayoutMargin.topMargin = systemBars.top
            binding.gestureView.layoutParams.height = systemBars.bottom
            binding.scrollView.setPadding(
                0,
                116f.dpToPx(this).roundToInt(),
                0,
                (systemBars.top + systemBars.bottom + 75f.dpToPx(this)).roundToInt()
            )
            insets
        }

        // 配置 Cookie 管理器
        val cookieManager = CookieManager.getInstance()
        // 配置 WebView
        configureWebView()
        // 允许接受Cookie
        cookieManager.setAcceptCookie(true)
        // 允许第三方 Cookies
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                // 提取数据
                sectionName = data?.getStringExtra("section_name") ?: ""
                sectionUrl = data?.getStringExtra("section_url") ?: ""
                // 加载隐藏页面
                binding.webView.loadUrl(BASE_URL + sectionUrl)
                loadingDialog.show()
                // 设置自定义 Cookies
                setCookiesForDomain(BASE_URL + sectionUrl, Cookies)
                binding.tvSection.text = sectionName
            }
        }

        if (!::sectionUrl.isInitialized) {
            resultLauncher.launch(Intent(this, SelectSectionActivity::class.java))
        }
        binding.btnSection.setOnClickListener {
            resultLauncher.launch(Intent(this, SelectSectionActivity::class.java))
        }
        binding.btnPrefix.setOnClickListener {
            if (!::sectionUrl.isInitialized) {
                Toast.makeText(
                    this,
                    R.string.select_section_toast,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            showRadioDialog(this, prefixNameList)
        }
        binding.chkBold.setOnCheckedChangeListener { _, isChecked ->
            if (!isUserTextAction) return@setOnCheckedChangeListener
            applyStyle(Typeface.BOLD, binding.etContent, isChecked)
        }
        binding.chkItalic.setOnCheckedChangeListener { _, isChecked ->
            if (!isUserTextAction) return@setOnCheckedChangeListener
            applyStyle(Typeface.ITALIC, binding.etContent, isChecked)
        }
        binding.chkImage.setOnClickListener {
            checkPermission()
        }
        binding.btnPost.setOnClickListener {
            if (!::sectionUrl.isInitialized){
                Toast.makeText(
                    this,
                    R.string.select_section_toast,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            loadingDialog.show()
            val title = binding.etTitle.text ?: ""
            val content = binding.etContent.text
                ?.let {
                    convertRichTextToBBCode(it)
                }
                ?.lines()
                ?.joinToString("") {
                    "<p>$it</p>"
                }
                ?: ""
            if (title.length <= 5 || content.length <= 5) {
                Toast.makeText(this, R.string.little_content_toast, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            binding.tvBBContent.text = content
            val injectScript =
                """
                (function() {
                    // 使用 XPath 定位目标元素
                    const xpath = '//*[@id="yesSideBar"]/div/div/form/div[2]/div[1]/div/dl[1]/dd/div/div[2]/div';
                    const result = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null);
                    const targetElement = result.singleNodeValue;
                    if (targetElement) {
                        // 修改元素的值
                        targetElement.innerHTML = "$content";
                        // 触发输入事件（如果需要）
                        const event = new Event('input', { bubbles: true });
                        targetElement.dispatchEvent(event);
                    } else {
                        console.error("未找到目标元素");
                    }
                    const targetTitleElement = document.querySelector('input[name="title"]');
                    if (targetTitleElement) {
                        // 修改元素的值
                        targetTitleElement.value = "$title"; // 如果是富文本编辑器
                        // 触发输入事件（如果需要）
                        const event = new Event('input', { bubbles: true });
                        targetTitleElement.dispatchEvent(event);
                    } else {
                        console.error("未找到目标元素");
                    }
                    var postBtn = document.getElementById('postbutton');
                    postBtn.click();
                })();
                """.trimIndent()
            binding.webView.evaluateJavascript(
                injectScript, null
            )
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // 页面加载完成
                super.onPageFinished(view, url)
                prefixIdList.clear()
                prefixNameList.clear()
                pitchPrefixId = 0
                pitchPrefixName = "(无前缀)"
                binding.tvPrefix.text = pitchPrefixName
                loadingDialog.dismiss()

                binding.webView.evaluateJavascript("document.documentElement.outerHTML") { html ->
                    val unescapedHtml = html.unescapeHtml()
                    val document = Jsoup.parse(unescapedHtml)
                    addFileLink = document
                        .getElementsByClass("button--link js-attachmentUpload button button--icon button--icon--attach")
                        .first()
                        ?.attr("href")
                        ?: ""
                    xfToken = document
                        .select("input[name=_xfToken]")
                        .first()
                        ?.attr("value") ?: throw Exception("CSRF token not found")
                    // 获取 Cookie
                    cookieString = cookieManager.getCookie(url)
                    val prefixSelect = document
                        .getElementsByClass("js-prefixSelect u-noJsOnly input")
                        .first()
                        ?.getElementsByTag("option")
                    prefixSelect?.forEach {
                        prefixIdList.add(it.attr("value").toInt())
                        prefixNameList.add(it.text())
                    }
                }
            }
            fun String.unescapeHtml(): String {
                return this.replace("\\u003C", "<")
                    .replace("\\\"", "\"")
                    .replace("\\'", "'")
                    .replace("\\\\", "\\")
            }
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                return handleUrl(url)
            }
            private fun handleUrl(url: String?): Boolean {
                if (url?.startsWith("https://www.uotan.cn/threads/") == true) {
                    loadingDialog.dismiss()
                    Toast.makeText(this@PostArticleActivity, R.string.published_successfully, Toast.LENGTH_SHORT).show()
                    this@PostArticleActivity.finish()
                    return false
                }
                Toast.makeText(this@PostArticleActivity, R.string.publication_failed, Toast.LENGTH_SHORT).show()
                return true
            }
        }
        binding.etContent.onSelectionChanged = { hasSelection ->
            if (hasSelection) {
                updateCheckboxStates()
            } else {
                // 无选中时取消选中
                binding.chkBold.isChecked = false
                binding.chkItalic.isChecked = false
            }
        }
    }

    private fun applyStyle(style: Int, editText: SelectableEditText, open: Boolean) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start >= end) return
        val spannable = editText.text as Spannable
        // 分割选中范围，排除换行符
        val ranges = splitRanges(editText.text!!, start, end)
        if (open) {
            // 为每个子范围添加样式
            ranges.forEach { (subStart, subEnd) ->
                spannable.setSpan(StyleSpan(style), subStart, subEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else {
            // 移除每个子范围的样式
            ranges.forEach { (subStart, subEnd) ->
                val spans = spannable.getSpans(subStart, subEnd, StyleSpan::class.java)
                spans?.forEach { span ->
                    if (span.style == style) {
                        spannable.removeSpan(span)
                    }
                }
            }
        }
        editText.setSelection(start, end)
    }

    // 根据换行符分割选中范围
    private fun splitRanges(text: CharSequence, start: Int, end: Int): List<Pair<Int, Int>> {
        val ranges = mutableListOf<Pair<Int, Int>>()
        var currentStart = start
        for (i in start until end) {
            if (text[i] == '\n') {
                if (currentStart < i) { // 有效区间（非空）
                    ranges.add(currentStart to i)
                }
                currentStart = i + 1 // 跳过换行符
            }
        }
        // 添加最后一个区间
        if (currentStart < end) {
            ranges.add(currentStart to end)
        }
        return ranges
    }

    private fun isSelectionStyled(editText: SelectableEditText, style: Int): Boolean {
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start >= end) return false
        val spannable = editText.text
        val spans = spannable?.getSpans(start, end, StyleSpan::class.java)
        if (spans != null) {
            for (span in spans) {
                if (span.style == style) {
                    val spanStart = spannable.getSpanStart(span)
                    val spanEnd = spannable.getSpanEnd(span)
                    if (spanStart <= start && spanEnd >= end) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun updateCheckboxStates() {
        isUserTextAction = false // 禁用事件触发
        binding.chkBold.isChecked = isSelectionBold(binding.etContent)
        binding.chkItalic.isChecked = isSelectionItalic(binding.etContent)
        isUserTextAction = true // 恢复事件监听
    }

    // 检查加粗
    private fun isSelectionBold(editText: SelectableEditText): Boolean {
        return isSelectionStyled(editText, Typeface.BOLD)
    }

    // 检查斜体
    private fun isSelectionItalic(editText: SelectableEditText): Boolean {
        return isSelectionStyled(editText, Typeface.ITALIC)
    }

    private fun convertRichTextToBBCode(text: CharSequence): String {
        val spannable = SpannableString(text)
        val builder = StringBuilder()
        var cursor = 0
        // 创建区间队列（起始位置，结束位置，样式集合）
        val intervals = mutableListOf<Triple<Int, Int, MutableSet<String>>>()
        // 收集有效Span并合并区间
        listOf(StyleSpan::class.java, URLSpan::class.java, ImageSpan::class.java).forEach { spanClass ->
            spannable.getSpans(0, spannable.length, spanClass).forEach { span ->
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                when (span) {
                    is StyleSpan -> {
                        val style = when (span.style) {
                            Typeface.BOLD -> "b"
                            Typeface.ITALIC -> "i"
                            else -> null
                        }
                        style?.let { addToInterval(intervals, start, end, it) }
                    }
                    is URLSpan -> {
                        addToInterval(intervals, start, end, "url:${span.url}")
                    }
                    is CustomImageSpan -> {
                        val picUrl = pictureMap[span.picName] ?: ""
                        addToInterval(intervals, start, end, "img:$picUrl")
                    }
                }
            }
        }
        // 按起始升序+结束降序排序
        intervals.sortWith(compareBy(
            { it.first },
            { -it.second }
        ))
        // 样式优先级映射（数值越小优先级越高）
        val stylePriority = mapOf(
            "img" to 0,
            "b" to 1,
            "i" to 2,
            "url" to 3
        )
        intervals.forEach { (start, end, styles) ->
            // 处理前置文本
            if (start > cursor) {
                builder.append(spannable.subSequence(cursor, start))
                cursor = start
            }

            // 生成嵌套标签
            val content = spannable.subSequence(start, end)
            val sortedStyles = styles.sortedBy { stylePriority[it.split(":").first()] }
            val paragraphs = content.split("\n") // 按换行符拆分段落
            paragraphs.forEachIndexed { index, paragraph ->
                // 添加开启标签
                sortedStyles.forEach { style ->
                    when {
                        style.startsWith("url:") -> {
                            val url = style.substringAfter(":")
                            builder.append("[url=$url]")
                        }
                        style.startsWith("img:") -> {
                            val url = style.substringAfter(":")
                            builder.append("[img]${BASE_URL + url}[/img]")
                        }
                        else -> {
                            builder.append("[$style]")
                        }
                    }
                }
                builder.append(paragraph)
                // 添加关闭标签
                sortedStyles.reversed().forEach { style ->
                    when {
                        style.startsWith("img:") -> { }
                        style.startsWith("url:") -> {
                            builder.append("[/url]")
                        }
                        else -> {
                            builder.append("[/$style]")
                        }
                    }
                }
                // 在段落间插入换行符（不在标签内）
                if (index < paragraphs.lastIndex) builder.append('\n')
            }
            cursor = maxOf(cursor, end)
        }
        // 处理尾部文本
        if (cursor < spannable.length) {
            builder.append(spannable.subSequence(cursor, spannable.length))
        }
        return builder.toString()
    }

    private fun addToInterval(
        intervals: MutableList<Triple<Int, Int, MutableSet<String>>>,
        newStart: Int,
        newEnd: Int,
        style: String
    ) {
        val iterator = intervals.listIterator()
        var added = false

        while (iterator.hasNext()) {
            val (start, end, styles) = iterator.next()
            when {
                // 区间完全重叠
                newStart == start && newEnd == end -> {
                    styles.add(style)
                    added = true
                    break
                }
                // 新区间在现有区间内
                newStart >= start && newEnd <= end -> {
                    iterator.add(Triple(newStart, newEnd, mutableSetOf(style)))
                    added = true
                    break
                }
            }
        }

        if (!added) {
            intervals.add(Triple(newStart, newEnd, mutableSetOf(style)))
        }
    }

    /** 以下为模拟点击部分 **/
    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
        }
    }

    private fun setCookiesForDomain(url: String, cookiesMap: Map<String, String>) {
        // 设置 CookieManager
        val cookieManager = CookieManager.getInstance()
        // 构建 Cookie
        cookiesMap.forEach { (key, value) ->
            // 构建符合规范的 Cookie 字符串
            val cookieString = buildString {
                // 基础键值对
                append("$key=$value")
            }
            // 设置 Cookie
            cookieManager.setCookie(url, cookieString)
        }
        // 同步 Cookies
        cookieManager.flush()
    }

    // 检查权限是否已授予
    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE_PERMISSION)
        } else {
            // 已授权，启动图片选择
            openImagePicker()
        }
    }

    // 处理权限请求结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            Toast.makeText(
                this,
                "需要权限才能选择图片",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // 实例化 OkHttpClient
            val client = OkHttpClient()
            // 将 URI 转换为 Bitmap
            val bitmap = uriToBitmap(it)
            // 获取文件路径
            val filePath = getPathFromUri(uri)
            // 获取文件
            val file = File(filePath)
            // 创建 MultipartBody
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload", file.name,
                    file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
                .addFormDataPart("type", "post")
                .addFormDataPart("context[node_id]",
                    """node_id]=(\d+)"""
                        .toRegex()
                        .find(addFileLink)
                        ?.groupValues
                        ?.get(1)
                        ?: ""
                )
                .addFormDataPart("hash",
                    """hash=([a-f0-9]+)"""
                        .toRegex()
                        .find(addFileLink)
                        ?.groupValues
                        ?.get(1)
                        ?: ""
                )
                .addFormDataPart("_xfToken", xfToken) // 替换为实际的 token
                .build()
            // 创建 Request
            val request = Request.Builder()
                .url(BASE_URL + addFileLink)
                .addHeader("Cookie", cookieString)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Origin", BASE_URL)
                .addHeader("Referer", addFileLink)
                .addHeader(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .addHeader("Connection", "keep-alive")
                .post(requestBody)
                .build()
            // 执行请求
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Toast.makeText(
                        this@PostArticleActivity,
                        "图片上传失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        Toast.makeText(
                            this@PostArticleActivity,
                            "图片上传失败",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    val document = response.body?.string() ?: ""
                    val picName = Jsoup.parse(document)
                        .getElementsByClass("js-attachmentFile file")
                        .last()
                        ?.getElementsByClass("file-name")
                        ?.first()
                        ?.text()
                        ?: ""
                    val picUrl = Jsoup.parse(document)
                        .getElementsByClass("js-attachmentFile file")
                        .last()
                        ?.getElementsByClass("file-preview js-attachmentView")
                        ?.first()
                        ?.attr("href")
                        ?: ""
                    pictureMap[picName] = picUrl
                    // 在主线程插入图片到EditText
                    runOnUiThread {
                        insertImageIntoEditText(bitmap, picName)
                    }
                }
            })
        }
    }
    private fun getPathFromUri(uri: Uri): String {
        return getPathFromUriWithResolver(uri) ?: (uri.path ?: "")
    }

    private fun getPathFromUriWithResolver(uri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(this, uri, proj, null, null, null)
        val cursor = loader.loadInBackground()
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else {
            null
        }
    }

    // 打开图片选择器
    private fun openImagePicker() {
        // 使用 pickImageLauncher 启动图片选择
        pickImageLauncher.launch("image/*")
    }

    // 将 URI 转换为 Bitmap
    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 将选择的图片插入至 EditText
     */
    private fun insertImageIntoEditText(bitmap: Bitmap?, picName: String) {
        // 防空
        bitmap?.let {
            // 获取当前 EditText 的内容
            val spannable = SpannableStringBuilder(binding.etContent.text)
            // 转换选择的图片
            val drawable = bitmap.toDrawable(resources)
            // 获取 EditText 最大宽度
            val targetWidth = binding.etContent.width - binding.etContent.paddingLeft - binding.etContent.paddingRight - 10
            // 计算缩小/放大的倍数
            val scale = targetWidth.toFloat() / drawable.intrinsicWidth
            // 计算缩小/放大后的高度
            val scaledHeight = (drawable.intrinsicHeight * scale).toInt()
            // 缩放 Drawable
            drawable.setBounds(0, 0, targetWidth, scaledHeight)
            // 创建 ImageSpan
            val imageSpan = CustomImageSpan(drawable, picName)
            // 获取 EditText 当前光标位置
            val cursorPos = binding.etContent.selectionStart
            // 插入占位符
            spannable.insert(cursorPos, " ")
            // 应用 Span
            spannable.setSpan(imageSpan, cursorPos, cursorPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            // 换行
            spannable.append("\n")
            // 更新 EditText
            binding.etContent.text = spannable
            // 移动 EditText 光标至图片之后下一行
            binding.etContent.setSelection(cursorPos + 2)
        }
    }
    // 自定义ImageSpan，携带图片名称
    class CustomImageSpan(drawable: Drawable, val picName: String) : ImageSpan(drawable)

    class PrefixListAdapter(
        private val context: Context,
        private val prefixList: List<String>,
        private val onItemClick: (Int, String, Int) -> Unit
    ) : BaseAdapter() {
        override fun getCount(): Int = prefixList.size
        override fun getItem(position: Int): Any = prefixList[position]
        override fun getItemId(position: Int): Long = position.toLong()

        @SuppressLint("Recycle", "ResourceType")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_radio_select, parent, false)
            val bkgItem = view.findViewById<View>(R.id.background)
            val tvItem = view.findViewById<TextView>(R.id.tvItem)
            val chkRadio = view.findViewById<CheckBox>(R.id.checkBox)
            tvItem.text = prefixList[position]
            val typedArray = context.obtainStyledAttributes(intArrayOf(R.attr.colorFilledButtonNormal, R.attr.colorBackground, R.attr.colorOnBackgroundPrimary))
            val red = typedArray.getColor(0, Color.RED)
            val colorBackground = typedArray.getColor(1, Color.RED)
            val colorOnBackgroundPrimary = typedArray.getColor(2, Color.RED)
            if (position == pitchPrefixPosition) {
                bkgItem.setBackgroundColor(red)
                bkgItem.alpha = 0.15f
                tvItem.setTextColor(red)
                chkRadio.isChecked = true
            } else {
                bkgItem.setBackgroundColor(colorBackground)
                bkgItem.alpha = 1.0f
                tvItem.setTextColor(colorOnBackgroundPrimary)
                chkRadio.isChecked = false
            }
            view.setOnClickListener {
                onItemClick(prefixIdList[position], prefixNameList[position], position)
            }
            return view
        }
    }

    @SuppressLint("InflateParams")
    fun showRadioDialog(context: Context, items: List<String>) {
        val choicePrefixDialog = BottomDialog
            .build(object : OnBindView<BottomDialog?>(R.layout.dialogx_select_prefix) {
                override fun onBind(dialog: BottomDialog?, v: View) {
                    val list = v.findViewById<ScrollControllerListView>(R.id.listView)
                    list.adapter = PrefixListAdapter(context, items) { id, name, position ->
                        pitchPrefixId = id
                        pitchPrefixName = name
                        pitchPrefixPosition = position
                        dialog?.dismiss()
                    }
                    list?.apply {
                        // 设置滚动边界检测
                        setOnScrollListener(object : AbsListView.OnScrollListener {
                            override fun onScrollStateChanged(view: AbsListView, state: Int) {
                                if (state == SCROLL_STATE_IDLE) {
                                    lockScroll = isAtTop // 滚动停止时更新锁定状态
                                }
                            }
                            override fun onScroll(v: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
                        })
                    }
                }
            })
            .setDialogLifecycleCallback(object : DialogLifecycleCallback<BottomDialog>() {
                override fun onDismiss(dialog: BottomDialog?) {
                    super.onDismiss(dialog)
                    binding.tvPrefix.text = pitchPrefixName
                    val injectScript = """
                        (function() {
                            // 获取目标元素
                            const selectEl = document.querySelector('select[name="prefix_id"]');
                            if(!selectEl) return;
                            // 设置值并触发事件
                            selectEl.value = '$pitchPrefixId';
                            // 创建兼容性事件
                            const createEvent = (type) => {
                                let event;
                                if(typeof Event === 'function') {
                                    event = new Event(type, { bubbles: true });
                                } else { // 兼容旧版浏览器
                                    event = document.createEvent('Event');
                                    event.initEvent(type, true, false);
                                }
                                return event;
                            }
                            // 同时触发input和change事件
                            ['input', 'change'].forEach(eventType => {
                                selectEl.dispatchEvent(createEvent(eventType));
                            });
                            // 强制更新UI显示
                            const displaySpan = document.querySelector('.js-activePrefix');
                            if(displaySpan) {
                                // 模拟用户点击选择器
                                const clickEvent = createEvent('click');
                                selectEl.parentElement.dispatchEvent(clickEvent);
                                // 延迟更新文本显示
                                setTimeout(() => {
                                    displaySpan.textContent = selectEl.options[selectEl.selectedIndex].text;
                                }, 50);
                            }
                        })();
                        """.trimIndent()
                    binding.webView.evaluateJavascript(
                        injectScript, null
                    )
                }
            })
        choicePrefixDialog.show()
    }
}