package com.comet.nfcreader.reader.data.repository

import com.comet.nfcbasedmdm.common.storage.LocalStorage
import com.comet.nfcreader.reader.data.model.ServerData

// preference Datastore 활용한 repo
class PreferenceServerDataRepository(private val localStorage: LocalStorage) : ServerDataRepository {

    companion object {
        private const val SERVER_IP_KEY = "SERVER_IP"
    }

    override suspend fun loadData(): ServerData? {
        val ip = localStorage.getString(SERVER_IP_KEY, "")
        return if (ip.isEmpty())
            null
        else
            ServerData(ip)
    }

    override suspend fun saveData(serverData: ServerData) {
        localStorage.putString(SERVER_IP_KEY, serverData.serverIp)
    }
}