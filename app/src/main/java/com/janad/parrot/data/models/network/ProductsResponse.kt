package com.janad.parrot.data.models.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponse(
    val page: Int,
    val limit: Int,
    val results: List<Product>
)

@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val price: String,
    @SerialName("created_at") val createdAt: String,
    val media: List<Media>
)

@Serializable
data class Media(
    val id: Int?=null,
    @SerialName("file_name") val fileName: String?=null,
    @SerialName("file_path") val filePath: String?=null,
    @SerialName("file_type") val fileType: String?=null,
)
