package com.gustate.uotan.resource.ui.details.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.github.iielse.imageviewer.core.Photo
import com.github.iielse.imageviewer.core.SimpleDataProvider
import com.gustate.uotan.article.ContentAdapter
import com.gustate.uotan.article.ContentBlock
import com.gustate.uotan.article.HtmlParse
import com.gustate.uotan.article.imageviewer.ImageLoader
import com.gustate.uotan.article.imageviewer.ImageTransformer
import com.gustate.uotan.databinding.FragmentResDetailsBinding
import com.gustate.uotan.resource.ui.details.ResDetailsViewModel
import com.gustate.uotan.threads.data.model.ThreadPhoto
import com.gustate.uotan.utils.Utils.dpToPx
import kotlin.math.roundToInt

class ResDetailsFragment : Fragment() {

    // ResDetailsFragment 私有变量
    // 可空的视图绑定，请在 Create 时初始化，Destroy 时置空
    private var _binding: FragmentResDetailsBinding? = null
    // 对视图绑定的非空访问，仅在生命周期有效时可用
    private val binding: FragmentResDetailsBinding get() = _binding!!
    private val viewModel by activityViewModels<ResDetailsViewModel>()
    private val pictureList = mutableListOf<Photo>()
    // (延迟实例化) 资源信息 (文章) 适配器
    private lateinit var detailsAdapter: ContentAdapter

    /**
     * 实例化用户界面视图
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 初始化视图绑定
        _binding = FragmentResDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * 实例化用户界面视图后
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val bottomMenuHeight = context?.let { 65f.dpToPx(it).roundToInt() } ?: 0
            binding.rvDetails.setPadding(
                binding.rvDetails.paddingLeft, binding.rvDetails.paddingTop,
                binding.rvDetails.paddingRight, systemBars.bottom + bottomMenuHeight)
            insets
        }

        detailsAdapter = ContentAdapter().apply {
            onImageClick = { id, url ->
                showPictureViewer(id.toLong(), url)
            }
        }
        binding.rvDetails.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDetails.adapter = detailsAdapter

        viewModel.details.observe(viewLifecycleOwner) {
            val content = HtmlParse.parse(it.content)
            detailsAdapter.updateContent(content)
            content.forEachIndexed { index, contentBlock ->
                if (contentBlock is ContentBlock.ImageBlock) {
                    pictureList.add(ThreadPhoto(contentBlock.src, index.toLong()))
                }
            }
        }
    }

    /**
     * 启动图片查看器
     * @param position 图片索引 用于绑定动画
     * @param url 图片链接
     */
    private fun showPictureViewer(position: Long, url: String) {
        val clickedData: Photo = ThreadPhoto(url, position)
        val builder = ImageViewerBuilder(
            context = requireContext(),
            dataProvider = SimpleDataProvider(clickedData, pictureList),        // 一次性全量加载
            imageLoader = ImageLoader(),                                        // 实现对数据源的加载
            transformer = ImageTransformer(),                                   // 设置过渡动画的配对
        )
        builder.show()
    }

    /**
     * 销毁时
     */
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}