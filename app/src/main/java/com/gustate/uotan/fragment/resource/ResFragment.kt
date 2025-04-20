package com.gustate.uotan.fragment.resource

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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.anim.TitleAnim
import com.gustate.uotan.databinding.FragmentResBinding
import com.gustate.uotan.utils.Utils.Companion.BASE_URL
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.parse.resource.ResourceItem
import com.gustate.uotan.utils.parse.resource.ResourceRecommendItem
import kotlin.math.roundToInt

class ResFragment : Fragment() {

    private var _binding: FragmentResBinding? = null
    private val binding: FragmentResBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentResBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(view.rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarView.updateLayoutParams<ViewGroup.LayoutParams> { height = systemBars.top }
            binding.coordinatorLayout.setPadding(
                systemBars.left,
                (systemBars.top + 60f.dpToPx(requireContext())).roundToInt(),
                systemBars.right,
                0
            )
            TitleAnim(
                binding.title,
                binding.bigTitle,
                (systemBars.top + 60f.dpToPx(requireContext())),
                systemBars.top.toFloat()
            )
            insets
        }
        binding.viewPager.adapter = ResourceViewPagerAdapter(this)
        binding.bigTitleTrends.setOnClickListener{ binding.viewPager.setCurrentItem(0, true) }
        binding.bigTitleCategorize.setOnClickListener{ binding.viewPager.setCurrentItem(1, true) }
        binding.titleTrends.setOnClickListener{ binding.viewPager.setCurrentItem(0, true) }
        binding.titleCategorize.setOnClickListener{ binding.viewPager.setCurrentItem(1, true) }
        // 底栏按钮默认颜色
        val normalColor = getThemeColor(requireContext(), R.attr.colorOnBackgroundSecondary)
        // 底栏按钮选择颜色
        val selectedColor = getThemeColor(requireContext(), R.attr.colorOnBackgroundPrimary)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.bigTitleTrends.setTextColor(selectedColor)
                        binding.bigTitleCategorize.setTextColor(normalColor)
                        binding.titleTrends.setTextColor(selectedColor)
                        binding.titleCategorize.setTextColor(normalColor)
                    }
                    1 -> {
                        binding.bigTitleTrends.setTextColor(normalColor)
                        binding.bigTitleCategorize.setTextColor(selectedColor)
                        binding.titleTrends.setTextColor(normalColor)
                        binding.titleCategorize.setTextColor(selectedColor)
                    }
                }
            }
        })
    }

    class ResourceViewPagerAdapter(fragment: Fragment):
        FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle){
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int) = when(position) {
            0 -> TrendsResourceFragment()
            1 -> TypeResourceFragment()
            else -> throw IllegalArgumentException()
        }
    }

    class TrendsResourceAdapter(): RecyclerView.Adapter<TrendsResourceAdapter.ViewHolder>() {
        private val trendsList = mutableListOf<ResourceRecommendItem>()
        // 点击监听接口
        var onItemClick: ((ResourceRecommendItem) -> Unit)? = null

        fun addAll(newItems: MutableList<ResourceRecommendItem>) {
            val startPosition = trendsList.size
            trendsList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val cover: ImageView = view.findViewById(R.id.cover)
            val title: TextView = view.findViewById(R.id.title)
            val describe: TextView = view.findViewById(R.id.describe)
            val updateTime: TextView = view.findViewById(R.id.update_time)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_trends_resource_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = trendsList[position]
            Glide
                .with(holder.itemView.context)
                .load(BASE_URL + content.cover)
                .into(holder.cover)
            holder.title.text = content.title
            holder.describe.text = content.version
            holder.updateTime.text = content.updateTime
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(content)
            }
        }

        override fun getItemCount(): Int = trendsList.size

    }

    /**
     * 资源列表适配器
     * @property resourceList 适配器加载的资源列表
     * @property onItemClick 项目单击接口
     * @property addAll 把内容添加到资源列表并通知更新
     * @see ResourceItem 资源项
     */
    class ResourceAdapter(): RecyclerView.Adapter<ResourceAdapter.ViewHolder>() {

        private val resourceList = mutableListOf<ResourceItem>()

        var onItemClick: ((ResourceItem) -> Unit)? = null

        fun addAll(newItems: MutableList<ResourceItem>) {
            val startPosition = resourceList.size
            resourceList.addAll(newItems)
            notifyItemRangeInserted(startPosition, newItems.size)
        }

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
            val avatar: ImageView = view.findViewById(R.id.cover)
            val title: TextView = view.findViewById(R.id.title)
            val describe: TextView = view.findViewById(R.id.describe)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_latest_resource_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val content = resourceList[position]
            Glide
                .with(holder.itemView.context)
                .load(BASE_URL + content.cover)
                .into(holder.avatar)
            holder.title.text = content.title
            holder.describe.text = content.version
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(content)
            }
        }

        override fun getItemCount(): Int = resourceList.size

    }

}