package com.uotan.forum.ui.view.input

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