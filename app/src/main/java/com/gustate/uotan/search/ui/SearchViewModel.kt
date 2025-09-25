package com.gustate.uotan.search.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.search.data.model.SearchItem
import com.gustate.uotan.search.data.parse.SearchParse
import com.gustate.uotan.utils.Utils.baseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {

    private val sp = SearchParse()

    private var isLoading = false
    private var currentPage = 1
    private var totalPages = 1
    private var nextPageUrl = ""
    private var searchContent = ""

    private val _isLastPage = MutableLiveData(false)
    val isLastPage get() = _isLastPage
    private val _searchList = MutableLiveData<MutableList<SearchItem>>()
    val searchList get() = _searchList

    fun loadData(
        content: String,
        isInitOrRefresh: Boolean,
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        if (isInitOrRefresh) {
            _isLastPage.value = false
            currentPage = 1
            totalPages = 1
        }
        if (isLoading || _isLastPage.value == true) return
        isLoading = true
        viewModelScope.launch {
            searchContent = content
            try {
                if (currentPage == 1) {
                    val searchResult = sp.parseSearchInfoPage(content, currentPage.toString())
                    val newList = (_searchList.value ?: mutableListOf()).toMutableList()
                    newList.addAll(searchResult.items)
                    _searchList.value = newList
                    currentPage += 1
                    totalPages = searchResult.totalPage
                    nextPageUrl = searchResult.nextPageUrl
                    _isLastPage.value = currentPage > totalPages
                } else {
                    sp.parseSearchInfo(
                        baseUrl + nextPageUrl, onSuccess = { searchResult ->
                            val newList = (_searchList.value ?: mutableListOf()).toMutableList()
                            newList.addAll(searchResult.items)
                            _searchList.value = newList
                            currentPage += 1
                            totalPages = searchResult.totalPage
                            nextPageUrl = searchResult.nextPageUrl
                            _isLastPage.value = currentPage > totalPages
                        }, onThrowable = {
                            throw it
                        }
                    )
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onException(e)
                    isLoading = false
                }
            }
        }
    }
}