package com.gustate.uotan.message.ui.message

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.message.data.model.AllMessage
import com.gustate.uotan.message.data.repository.MessageRepository
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