package com.comet.nfcreader.reader.server.type

// 서버 상태 관련 ENUM
/**
 * @property OK 성공
 * @property ERROR 오류
 * @property DATA_NOT_INIT IP 없음
 */
enum class ResponseStatus {
    OK, ERROR, DATA_NOT_INIT
}