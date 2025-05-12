package com.gustate.uotan.message.ui.personal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.message.data.model.PersonalLetter
import com.gustate.uotan.message.data.repository.PersonalLetterRepository
import kotlinx.coroutines.launch

class PersonalLetterViewModel: ViewModel() {

    private val personalLetterRepository = PersonalLetterRepository()

    private val _personalLetterList = MutableLiveData<MutableList<PersonalLetter>>()
    val personalLetterList: MutableLiveData<MutableList<PersonalLetter>> get() = _personalLetterList

    private val _currentPage = MutableLiveData(0)
    val currentPage: MutableLiveData<Int> get() = _currentPage

    private val _isLastPage = MutableLiveData(false)
    val isLastPage: MutableLiveData<Boolean> get() = _isLastPage

    fun loadInitialData(userId: String) {
        viewModelScope.launch {
            try {
                currentPage.value = 1
                val pll = personalLetterRepository.getPersonalLetterList(userId, currentPage.value!!)
                isLastPage.value = pll.first().totalPage == 1
                personalLetterList.value = pll
            } catch (e: Exception) {

            }
        }
    }

    fun loadMoreData(userId: String) {
        viewModelScope.launch {
            try {
                currentPage.value = (currentPage.value!! + 1)
                val pll = personalLetterRepository.getPersonalLetterList(userId, currentPage.value!!)
                if (pll.first().totalPage <= currentPage.value!!) {
                    isLastPage.value = true
                }
                personalLetterList.value?.addAll(pll) ?: pll
            } catch (e: Exception) {

            }
        }
    }
}