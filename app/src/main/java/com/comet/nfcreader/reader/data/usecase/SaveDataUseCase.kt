package com.comet.nfcreader.reader.data.usecase

import com.comet.nfcreader.reader.data.model.ServerData
import com.comet.nfcreader.reader.data.repository.ServerDataRepository

class SaveDataUseCase(private val serverDataRepository: ServerDataRepository) {

    suspend operator fun invoke(serverData: ServerData) {
        serverDataRepository.saveData(serverData)
    }
}