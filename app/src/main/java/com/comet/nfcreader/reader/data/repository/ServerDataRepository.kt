package com.comet.nfcreader.reader.data.repository

import com.comet.nfcreader.reader.data.model.ServerData

// 서버 정보 저장용 레포지토리
interface ServerDataRepository {

    suspend fun loadData() : ServerData?

    suspend fun saveData(serverData: ServerData)
}