package com.comet.nfcreader.reader.server.repository

import com.comet.nfcreader.reader.server.api.ServerAPI
import com.comet.nfcreader.reader.server.api.dto.TagRequestDTO
import com.comet.nfcreader.reader.server.api.dto.TagResponseDTO
import com.skydoves.sandwich.ApiResponse

// retrofit 활용한 repo
class RemoteTagRepository(private val serverAPI: ServerAPI) : TagRepository {

    override suspend fun tag(baseUrl: String, message: String): ApiResponse<TagResponseDTO> {
        return serverAPI.tag(baseUrl.plus("/mdm/request"), TagRequestDTO(message))
    }
}