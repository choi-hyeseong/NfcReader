package com.comet.nfcreader.reader.server.repository

import com.comet.nfcreader.reader.server.api.dto.TagResponseDTO
import com.skydoves.sandwich.ApiResponse

// 태깅 수행하는 레포지토리
interface TagRepository {

    suspend fun tag(baseUrl : String, message : String) : ApiResponse<TagResponseDTO>

}