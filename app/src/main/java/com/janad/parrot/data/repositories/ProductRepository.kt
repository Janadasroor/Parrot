package com.janad.parrot.data.repositories

import com.janad.parrot.data.api.ApiService
import com.janad.parrot.data.api.ModifyProductRequest
import com.janad.parrot.data.api.ProductRequest
import com.janad.parrot.data.api.ProductResponse
import com.janad.parrot.data.api.UploadResponse
import com.janad.parrot.data.models.network.ProductsResponse
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class ProductRepository @Inject constructor(private val api: ApiService) {
    suspend fun  getProducts(page: Int , limit: Int): Response<ProductsResponse> {
        return  api.getProducts(page , limit)
    }
    suspend fun postProduct(product: ProductRequest):Response<ProductResponse> {
    return  api.postProduct(product)
    }
    suspend fun updateProduct(product: ModifyProductRequest):Response<ProductResponse> {
    return  api.updateProduct(product)
    }
    suspend fun deleteProduct(id: Int):Response<ProductResponse> {
        return  api.deleteProduct(id)
    }
    suspend fun uploadImageFile( body:  MultipartBody.Part) :Response<UploadResponse> {
        return  api.uploadImage(body)
    }
    suspend fun uploadVideoFile( body:  MultipartBody.Part) :Response<UploadResponse> {
        return  api.uploadVideo(body)
    }
    suspend fun sendTestMsg(msg: String):Response<String> {
        return  api.sendTestMsg(mapOf("message" to msg))

    }

}