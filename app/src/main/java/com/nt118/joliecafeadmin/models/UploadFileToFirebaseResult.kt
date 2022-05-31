package com.nt118.joliecafeadmin.models


sealed class UploadFileToFirebaseResult<T>(
    val downloadUri: T? = null,
    val errorMessage: String? = null,
) {
    class Success<T>(downloadUri: T): UploadFileToFirebaseResult<T>(downloadUri)
    class Error<T>(data: T? = null, errorMessage: String?): UploadFileToFirebaseResult<T>(data, errorMessage)
    class Loading<T>: UploadFileToFirebaseResult<T>()
    class Idle<T>: UploadFileToFirebaseResult<T>()
}

