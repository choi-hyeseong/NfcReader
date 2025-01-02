package com.comet.nfcreader.reader.data.usecase

import com.comet.nfcreader.reader.data.model.ServerData
import com.comet.nfcreader.reader.data.repository.ServerDataRepository

class LoadDataUseCase(private val serverDataRepository: ServerDataRepository) {
    suspend operator fun invoke() : ServerData? {
        return serverDataRepository.loadData()
    }
}