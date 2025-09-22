
// API Service
package com.janad.parrot.data.api


import com.janad.parrot.data.models.network.ProductsResponse
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginReq):Response<LoginRes>
    @POST("auth/register")
    suspend fun register(@Body request: RegisterReq):Response<RegisterRes>
    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutReq):Response<LogoutRes>
    @POST("auth/refresh")
    fun refreshToken(@Body request: RefreshReq): Response<RefreshRes>
    @GET("product/get")
    suspend fun getProducts(@Query("page") page: Int,
                            @Query("limit") limit: Int): Response<ProductsResponse>
    @Multipart
    @POST("file/upload/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): Response<UploadResponse>
 @Multipart
    @POST("file/upload/video")
    suspend fun uploadVideo(
        @Part video: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): Response<UploadResponse>
    @POST("product/create")
    suspend fun postProduct(@Body request: ProductRequest): Response<ProductResponse>
    @PUT("product/update")
    suspend fun updateProduct(@Body request: ModifyProductRequest): Response<ProductResponse>
    @DELETE("product/delete/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<ProductResponse>
    @POST("test/msg")
    suspend fun sendTestMsg(@Body request: Map<String, String>): Response<String>
    companion object
}
@Serializable
data class ProductRequest(
    val title: String,
    val description: String,
    val price: Double,
    val mediaIds: List<Int>
)
@Serializable
data class ModifyProductRequest(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val mediaIds: List<Int>
)
@Serializable
data class ProductResponse(
    val message: String
)


@Serializable
data class UploadResponse(
    val message: String,
    val mediaId: Int
)
@Serializable
data class User(
    val id :String,
    val email:String
)
@Serializable
data class LoginRes(
    val  accessToken:String,
    val  refreshToken:String,
    val  user:User,
    val error:String? =null
)
@Serializable
data class LoginReq(
    val email :String,
    val password :String

)
@Serializable
data class LogoutReq(
    val refreshToken :String,

)
@Serializable
data class RefreshReq (
    val refreshToken :String,

)
@Serializable
data class RegisterRes(
    val  accessToken:String,
    val  refreshToken:String,
    val  user:User,
    val error:String? =null
)
@Serializable
data class LogoutRes(
    val  message:String,
)
@Serializable
data class RefreshRes(
    val  accessToken:String
//    val  refreshToken:String
    ,)
@Serializable
data class RegisterReq(
    val email :String,
    val password :String,

    )