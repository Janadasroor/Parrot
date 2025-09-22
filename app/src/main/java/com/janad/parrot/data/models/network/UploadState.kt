package com.janad.parrot.data.models.network

sealed class UploadState {
        object Idle : UploadState() // initial state
        object Loading : UploadState() // uploading
        object Success : UploadState() // all uploads completed
        data class Error(val message: String) : UploadState() // failed
    }