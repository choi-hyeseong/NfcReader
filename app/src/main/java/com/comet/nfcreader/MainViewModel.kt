package com.comet.nfcreader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.comet.nfcreader.reader.data.model.ServerData
import com.comet.nfcreader.reader.data.usecase.LoadDataUseCase
import com.comet.nfcreader.reader.data.usecase.SaveDataUseCase
import com.comet.nfcreader.reader.server.type.ResponseStatus
import com.comet.nfcreader.reader.server.usecase.TagUseCase
import com.skydoves.sandwich.onFailure
import com.skydoves.sandwich.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loadDataUseCase: LoadDataUseCase,
    private val saveDataUseCase: SaveDataUseCase,
    private val tagUseCase: TagUseCase
) : ViewModel() {

    // 응답용 enum
    val responseLiveData: MutableLiveData<ResponseStatus> = MutableLiveData()
    val serverIPLiveData : MutableLiveData<String> by lazy {
        MutableLiveData<String>().also { loadServerIP() }
    }

    // 태그 요청
    fun requestTagging(data: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val serverData: ServerData? = loadDataUseCase()
            if (serverData == null) {
                responseLiveData.postValue(ResponseStatus.DATA_NOT_INIT)
                return@launch
            }

            tagUseCase(serverData.serverIp, data).onSuccess {
                responseLiveData.postValue(ResponseStatus.OK)
            }.onFailure { responseLiveData.postValue(ResponseStatus.ERROR) }
        }
    }

    fun saveServerIP(data : String) {
        CoroutineScope(Dispatchers.IO).launch {
            saveDataUseCase(ServerData(data))
            serverIPLiveData.postValue(data) //알림용
        }
    }

    fun loadServerIP() {
        CoroutineScope(Dispatchers.IO).launch {
            val ip = loadDataUseCase()
            if (ip != null)
                serverIPLiveData.postValue(ip.serverIp)
        }
    }
}