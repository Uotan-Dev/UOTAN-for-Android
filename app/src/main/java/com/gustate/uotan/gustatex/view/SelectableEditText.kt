package com.gustate.uotan.gustatex.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class SelectableEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {
    var onSelectionChanged: ((hasSelection: Boolean) -> Unit)? = null

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChanged?.invoke(selStart != selEnd)
    }
}