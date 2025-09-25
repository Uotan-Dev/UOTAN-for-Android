package com.gustate.uotan.search.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.gustate.uotan.search.data.model.SearchItem
import com.gustate.uotan.search.data.parse.SearchParse

class SearchPagingSource(
    private val searchParse: SearchParse,
    private val query: String
) : PagingSource<Int, SearchItem>() {

    override fun getRefreshKey(state: PagingState<Int, SearchItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchItem> {
        return try {
            val page = params.key ?: 1
            val response = searchParse.parseSearchInfoPage(query, page.toString())

            LoadResult.Page(
                data = response.items,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (page < response.totalPage) page + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}