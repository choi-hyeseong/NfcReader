package com.comet.nfcreader.reader.server.api

import com.comet.nfcreader.reader.server.api.dto.TagRequestDTO
import com.comet.nfcreader.reader.server.api.dto.TagResponseDTO
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

// 서버 요청 api
interface ServerAPI {

    // 성공 여부만 확인
    // suspend 안붙여놓고 왜 response 안되나 싶었네..
    @POST
    suspend fun tag(@Url url : String, @Body data : TagRequestDTO) : ApiResponse<TagResponseDTO>
}