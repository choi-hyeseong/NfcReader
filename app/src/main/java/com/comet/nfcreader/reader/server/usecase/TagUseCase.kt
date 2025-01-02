package com.comet.nfcreader.reader.server.usecase

import com.comet.nfcreader.reader.server.api.dto.TagResponseDTO
import com.comet.nfcreader.reader.server.repository.TagRepository
import com.skydoves.sandwich.ApiResponse

/**
 * @see TagRepository.tag
 */
class TagUseCase(private val tagRepository: TagRepository) {

    suspend operator fun invoke(baseUrl : String, body : String) : ApiResponse<TagResponseDTO> {
        return tagRepository.tag(baseUrl, body)
    }
}