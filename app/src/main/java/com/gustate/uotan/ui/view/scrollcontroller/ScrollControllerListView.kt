package com.gustate.uotan.ui.view.scrollcontroller

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.AbsListView
import android.widget.ListView
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.interfaces.ScrollController

class ScrollControllerListView(context: Context, attrs: AttributeSet) : ListView(context, attrs),
    ScrollController {

    var lockScroll = false

    var isAtTop = true

    init {
        // 初始化时设置滚动监听器
        setOnScrollListener(object : OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, state: Int) {
                if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    lockScroll = isAtTop // 滚动停止时更新锁定状态
                }
            }

            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {}
        })
    }

    override fun isLockScroll(): Boolean = lockScroll

    override fun lockScroll(lockScroll: Boolean) {
        this.lockScroll = lockScroll
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return if (lockScroll && isAtTop) false else super.onTouchEvent(ev)
    }

    override fun getScrollDistance(): Int {
        isAtTop = when {
            isEmpty() -> true
            firstVisiblePosition > 0 -> false
            else -> getChildAt(0).top >= 0
        }
        return if (isAtTop) 0 else 1
    }

    override fun isCanScroll(): Boolean = true
}