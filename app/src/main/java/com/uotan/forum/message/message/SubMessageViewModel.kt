package com.uotan.forum.message.message

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uotan.forum.message.data.model.AllMessage
import com.uotan.forum.message.data.repository.MessageRepository
import kotlinx.coroutines.launch

class SubMessageViewModel: ViewModel() {

    private val messageRepository = MessageRepository()

    private val _noticeList = MutableLiveData<MutableList<AllMessage>>()
    val noticeList: MutableLiveData<MutableList<AllMessage>> get() = _noticeList

    fun loadInitialData() {
        viewModelScope.launch {
            try {
                val allMessage = messageRepository.getAllMessage()
                _noticeList.value = allMessage
            } catch (e: Exception) {

            }
        }
    }

}