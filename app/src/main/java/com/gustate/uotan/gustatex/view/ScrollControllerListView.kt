package com.gustate.uotan.gustatex.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ListView
import com.kongzue.dialogx.interfaces.ScrollController
import androidx.core.view.isEmpty

class ScrollControllerListView(context: Context, attrs: AttributeSet) : ListView(context, attrs),
    ScrollController {

    var lockScroll = false

    var isAtTop = true

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