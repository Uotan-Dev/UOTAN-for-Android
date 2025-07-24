package com.gustate.uotan.article.viewholder

import android.text.method.LinkMovementMethod
import android.view.Gravity.CENTER
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.R
import com.gustate.uotan.article.ContentBlock.ImageBlock
import com.gustate.uotan.article.ContentBlock.TableBlock
import com.gustate.uotan.article.ContentBlock.TableCellBlock
import com.gustate.uotan.article.ContentBlock.TextBlock
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import kotlin.math.roundToInt

class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tableLayout = itemView.findViewById<TableLayout>(R.id.tableLayout)

    fun bind(tableBlock: TableBlock) {
        tableLayout.removeAllViews()

        for (row in tableBlock.rows) {
            val tableRow = TableRow(tableLayout.context).apply {
                layoutParams = TableLayout.LayoutParams(
                    MATCH_PARENT, WRAP_CONTENT
                )
            }
            for (cell in row.cells) {
                val cellContainer = LinearLayout(tableLayout.context).apply {
                    orientation = LinearLayout.VERTICAL // 关键：垂直排列子视图
                    layoutParams = TableRow.LayoutParams(
                        0, MATCH_PARENT, 1f // 高度 WRAP_CONTENT 自适应内容
                    )
                    setBackgroundResource(R.drawable.gustatex_table_cell_border)
                    setPadding(
                        6f.dpToPx(tableLayout.context).roundToInt(),
                        6f.dpToPx(tableLayout.context).roundToInt(),
                        6f.dpToPx(tableLayout.context).roundToInt(),
                        6f.dpToPx(tableLayout.context).roundToInt()
                    )
                }
                renderTableCell(cellContainer, cell)
                tableRow.addView(cellContainer)
            }

            tableLayout.addView(tableRow)
        }
    }

    fun renderTableCell(container: ViewGroup, cell: TableCellBlock) {
        container.removeAllViews()

        cell.blocks.forEach { block ->
            when (block) {
                is TextBlock -> {
                    val tv = TextView(container.context).apply {
                        text = HtmlCompat.fromHtml(block.html, HtmlCompat.FROM_HTML_MODE_COMPACT)
                        movementMethod = LinkMovementMethod.getInstance()
                        gravity = CENTER
                    }
                    container.addView(tv)
                }
                is ImageBlock -> {
                    val iv = ImageView(container.context).apply {
                        Glide.with(this).load(block.src).into(this)
                    }
                    container.addView(iv)
                }
                else -> {
                    val tv = TextView(container.context).apply {
                        text = HtmlCompat.fromHtml(block.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                    container.addView(tv)
                }
            }
        }
    }
}