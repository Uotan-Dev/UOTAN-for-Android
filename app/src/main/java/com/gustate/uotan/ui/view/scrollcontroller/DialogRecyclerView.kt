package com.gustate.uotan.ui.view.scrollcontroller

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.RecyclerView
import com.kongzue.dialogx.interfaces.ScrollController

class DialogRecyclerView(context: Context, attrs: AttributeSet) : RecyclerView(context, attrs), ScrollController {

    private var lockScroll = false

    private var isAtTop = true

    override fun isLockScroll() = lockScroll

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
            computeVerticalScrollOffset() > 0 -> false
            else -> getChildAt(0).top >= 0
        }
        return if (isAtTop) 0 else 1
    }

    override fun isCanScroll() = true

    override fun onScrollStateChanged(state: Int) {
        if (state == SCROLL_STATE_IDLE) {
            lockScroll = isAtTop // 滚动停止时更新锁定状态
        }
    }

}