package com.janad.parrot.presentation.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.janad.parrot.data.repositories.AuthRepository
import com.janad.parrot.data.UserPreferences
import com.janad.parrot.data.api.LoginRes
import com.janad.parrot.data.api.LogoutRes
import com.janad.parrot.data.api.ModifyProductRequest
import com.janad.parrot.data.api.ProductRequest
import com.janad.parrot.data.api.RefreshRes
import com.janad.parrot.data.api.RegisterRes
import com.janad.parrot.data.models.network.Media
import com.janad.parrot.data.models.network.ProductsResponse
import com.janad.parrot.data.models.network.UploadState
import com.janad.parrot.data.repositories.ProductRepository
import com.janad.parrot.presentation.screens.main.MediaItem
import com.janad.parrot.presentation.screens.main.MediaType
import com.janad.parrot.presentation.screens.main.copyUriToFile
import com.janad.parrot.utils.ProgressRequestBody
import com.janad.parrot.utils.UploadNotificationHelper
import com.janad.parrot.utils.compressImageLegacy
import com.janad.parrot.utils.compressVideoFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    val userPreferences: UserPreferences
) : ViewModel() {
    val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val _modifyState =MutableStateFlow<UploadState>(UploadState.Idle)
    val modifyState: StateFlow<UploadState> = _modifyState
    private val _deleteState =MutableStateFlow<UploadState>(UploadState.Idle)
    val deleteState: StateFlow<UploadState> = _deleteState
    private val _accessToken =MutableStateFlow("")
    private val _refreshToken =MutableStateFlow("")
    val refreshToken : StateFlow<String> =_refreshToken


    private  val _loginResult =MutableStateFlow<LoginRes?>(null)
    open val loginResult: StateFlow<LoginRes?> = _loginResult
    private  val _logoutResult =MutableStateFlow<LogoutRes?>(null)
    val logoutResult: StateFlow<LogoutRes?> = _logoutResult

    private  val _registerResult =MutableStateFlow<RegisterRes?>(null)
    val registerResult: StateFlow<RegisterRes?> = _registerResult
     private  val _refreshResult =MutableStateFlow<RefreshRes?>(null)
        val refreshResult: StateFlow<RefreshRes?> = _refreshResult

    private val _authError = MutableStateFlow("")
    open val authError :StateFlow<String> =_authError
    private val _products = MutableStateFlow<ProductsResponse?>(null)
    val products: StateFlow<ProductsResponse?> = _products

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress/**
     * Clears the access and refresh tokens from both the local state and persistent storage.
     */
    private suspend fun clearTokens() {
        _accessToken.value = ""
        _refreshToken.value = ""
        userPreferences.clearLoginInfo()
    }
    /**
     * Refreshes the access token using the provided refresh token.
     * Updates the local state and persistent storage with the new access token.
     */
    fun refreshToken(refreshToken:String){
        viewModelScope.launch (Dispatchers.IO){
            try {
                val response = authRepository.refreshToken(refreshToken)
                if (response.isSuccessful) {
                    _refreshResult.value = response.body()
                //    userPreferences.saveRefreshToken(response.body()?.refreshToken ?: "")
                    userPreferences.saveAccessToken(response.body()?.accessToken ?: "")
                } else {
                   userPreferences.saveRefreshToken("")
                   _authError.value = response.errorBody()?.string() ?: "Unknown error"
                }
            }
            catch (e:Exception){

                _authError.value ="Network error: ${e.message}"
            }
        }
    }


    /**
     * Logs out the current user by clearing tokens and making a logout API call.
     * Updates the UI state accordingly.
     */
    fun logout(refreshToken: String) {
        viewModelScope.launch {
            try {
                userPreferences.clearLoginInfo()
                _loginResult.value = null
                _registerResult.value = null
                _authError.value = ""
                _accessToken.value = ""
                _refreshToken.value = ""
                val response = authRepository.logout(refreshToken)
                if (response.isSuccessful) {
                    _logoutResult.value = response.body()
                } else {

                    _authError.value = "Unknown error"
                }

            }
        catch (e:Exception){

            _authError.value ="Network error: ${e.message}"
        }
        }
    }
    /**
     * Logs in a user with the given email and password.
     * Saves the access and refresh tokens upon successful login.
     */
    open fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authRepository.login(email, password)
                if (response.isSuccessful) {
                    val loginRes = response.body()
                    _loginResult.value = loginRes
                    // Save tokens
                    _accessToken.value = loginRes?.accessToken ?: ""
                    _refreshToken.value = loginRes?.refreshToken ?: ""
                    userPreferences.saveAccessToken(_accessToken.value)
                    userPreferences.saveRefreshToken(_refreshToken.value)
                } else {
                    clearTokens()
                    _authError.value = response.body()?.error ?: "Unknown error"
                }
            } catch (e: Exception) {
                clearTokens()
                _authError.value = "Network error: ${e.message}"
            }
        }
    }
    /**
     * Registers a new user with the given email and password.
     * Saves the access and refresh tokens upon successful registration.
     */
    fun register(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = authRepository.register(email, password)
                if (response.isSuccessful) {
                    val registerRes = response.body()
                    _registerResult.value = registerRes
                    // Save tokens
                    _accessToken.value = registerRes?.accessToken ?: ""
                    _refreshToken.value = registerRes?.refreshToken ?: ""
                    userPreferences.saveAccessToken(_accessToken.value)
                    userPreferences.saveRefreshToken(_refreshToken.value)
                } else {
                    clearTokens()
                    _authError.value = response.body()?.error ?: "Unknown error"
                }
            } catch (e: Exception) {
                clearTokens()
                _authError.value = "Network error: ${e.message}"
            }
        }
    }
    /**
     * Fetches a list of products from the server.
     * Updates the UI state with the fetched products or an error message.
     */
    fun getProducts(page: Int = 1, limit: Int = 10){
        viewModelScope.launch {
            try {
                val response = productRepository.getProducts(page, limit)
                if (response.isSuccessful) {
                    _products.value = response.body()
                    Log.d("Product", response.body()?.results[0]?.title ?: "No Data")
                } else {
                    _authError.value = "Unknown error"
                    Log.d("Product", response.errorBody()?.string() ?: "No Data")
                }
            } catch (e: Exception) {
                Log.d("Product", e.message.toString())
                _authError.value = "Network error: ${e.message}"
            }
        }
    }

    private val _mediaIds = MutableStateFlow<List<Int>>(emptyList())
    val mediaIds: StateFlow<List<Int>> = _mediaIds
    /**
     * Uploads an image file to the server.
     * Optionally compresses the image before uploading if `isHD` is false.
     * Shows upload progress and completion/failure notifications.
     * @param context The application context.
     * @param imageFile The image file to upload.
     * @param notificationId A unique ID for the upload notification.
     * @param isHD Whether to upload the image in high definition (no compression).
     * @return The media ID of the uploaded image, or null if the upload failed.
     */
    suspend fun uploadImageFile(
        context: Context,
        imageFile: File,
        notificationId: Int,
        isHD: Boolean = false
    ): Int? {
        val notificationHelper = UploadNotificationHelper(context)

        // Track compressed file separately so we can delete later
        var compressedFile: File? = null

        return try {
            // 1. Decide which file to upload
            val fileToUpload: File = if (isHD) {
                imageFile
            } else {
                // Compress first
                compressedFile = compressImageLegacy(context, imageFile)
                compressedFile ?: imageFile // fallback if compression failed
            }

            // 2. Build multipart body with progress
            val requestFile = ProgressRequestBody(
                fileToUpload,
                "image/*",
                notificationHelper,
                notificationId
            )

            val body = MultipartBody.Part.createFormData("image", fileToUpload.name, requestFile)

            // 3. Upload
            val response = productRepository.uploadImageFile(body)

            if (response.isSuccessful) {
                notificationHelper.showCompletedNotification(notificationId, fileToUpload.name)
                response.body()?.mediaId
            } else {
                notificationHelper.showFailedNotification(notificationId, fileToUpload.name)
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            notificationHelper.showFailedNotification(notificationId, imageFile.name)
            null
        } finally {
            // 4. Clean up the compressed file to free space
            if (!isHD) {
                compressedFile?.let { file ->
                    if (file.exists()) file.delete()
                }
            }
        }
    }
    /**
     * Uploads a video file to the server.
     * Optionally compresses the video before uploading if `isHD` is false.
     * Shows upload progress and completion/failure notifications.
     * @param context The application context.
     * @param videoFile The video file to upload.
     * @param notificationId A unique ID for the upload notification.
     * @param isHD Whether to upload the video in high definition (no compression).
     * @return The media ID of the uploaded video, or null if the upload failed.
     */
    suspend fun uploadVideoFile(
        context: Context,
        videoFile: File,
        notificationId: Int,
        isHD: Boolean = false
    ): Int? {
        val notificationHelper = UploadNotificationHelper(context)

        // Track compressed file separately so we can delete later
        var compressedFile: File? = null

        return try {
            // 1. Decide which file to upload
            val fileToUpload: File = if (isHD) {
                videoFile
            } else {
                // Compress first
                compressedFile = compressVideo(
                    context,
                    Uri.fromFile(videoFile)
                )
                compressedFile ?: videoFile // fallback if compression failed
            }

            // 2. Build multipart body with progress
            val requestFile = ProgressRequestBody(
                fileToUpload,
                "video/*",
                notificationHelper,
                notificationId
            )

            val body = MultipartBody.Part.createFormData("video", fileToUpload.name, requestFile)

            // 3. Upload
            val response = productRepository.uploadVideoFile(body)

            if (response.isSuccessful) {
                notificationHelper.showCompletedNotification(notificationId, fileToUpload.name)
                response.body()?.mediaId
            } else {
                notificationHelper.showFailedNotification(notificationId, fileToUpload.name)
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            notificationHelper.showFailedNotification(notificationId, videoFile.name)
            null
        } finally {
            // 4. Clean up the compressed file to free space
            if (!isHD) {
                compressedFile?.let { file ->
                    if (file.exists()) file.delete()
                }
            }
        }
    }
    /**
     * Compresses a video file.
     * @param context The application context.
     * @param inputUri The URI of the video file to compress.
     * @return The compressed video file, or null if compression failed.
     */
    private suspend fun compressVideo(
        context: Context,
        inputUri: Uri
    ): File? {
        return try {
            val file = compressVideoFile(
                context = context,
                inputUri = inputUri,
            )
            file?.takeIf { it.exists() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    /**
     * Posts a new product with associated media (images/videos).
     * Uploads each media item concurrently and then posts the product details.
     * @param title The title of the product.
     * @param description The description of the product.
     * @param price The price of the product.
     * @param mediaList A list of [MediaItem] objects representing the media to upload.
     * @param context The application context.
     */
    fun postProductWithMedia(title: String, description: String, price: Double,mediaList: List<MediaItem>, context: Context) {
        viewModelScope.launch {
            try {
            _uploadState.value = UploadState.Loading
                val currentList = _mediaIds.value.toMutableList()


                // Launch all uploads concurrently
                val uploadJobs = mediaList.mapIndexed { index, mediaItem ->
                    async(Dispatchers.IO) {
                        val file = copyUriToFile(context, mediaItem.uri, mediaItem.type)
                        val notificationId = 1000 + index  // unique per file

                        val mediaId = when (mediaItem.type) {
                            MediaType.IMAGE -> uploadImageFile(context, file, notificationId,mediaItem.isHD)
                            MediaType.VIDEO -> uploadVideoFile(context, file, notificationId,mediaItem.isHD)
                        }

                        mediaId?.let { id ->
                            synchronized(currentList) { currentList.add(id) }
                            _mediaIds.value = currentList
                        }
                        file.delete()
                    }
                }
                uploadJobs.awaitAll()
             val success=   postProduct(title, description, price)

                // Wait for all uploads to complete
           uploadJobs.awaitAll()

            if(currentList.size == mediaList.size&&success){
                _uploadState.value = UploadState.Success
            }
                else{
                _uploadState.value = UploadState.Error("Upload failed")
            }
        }
        catch (e: Exception) {
            _uploadState.value = UploadState.Error(e.message ?: "Unknown error")
        }
        }
    }
    /**
     * Posts a product to the server.
     * @param title The title of the product.
     * @param description The description of the product.
     * @param price The price of the product.
     * @return True if the product was posted successfully, false otherwise.
     */
    suspend fun postProduct(title: String, description: String, price: Double): Boolean {

            try {
                val mediaIds = _mediaIds.value // collect current list
                val product = ProductRequest(
                    title = title,
                    description = description,
                    price = price,
                    mediaIds = mediaIds
                )

                val response = productRepository.postProduct(product)

                if (response.isSuccessful) {
                    println("Product posted: ${response.body()}")
                    // ✅ Clear media IDs after posting successfully
                    _mediaIds.value = emptyList()
                    return true
                } else {
                    println("Failed to post product: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Error posting product: ${e.message}")
            }
        return false
    }
    /**
     * Updates an existing product on the server.
     * @param productId The ID of the product to update.
     * @param title The new title of the product.
     * @param description The new description of the product.
     * @param price The new price of the product.
     * @param mediaList A list of [Media] objects representing the updated media for the product.
     */
    fun updateProduct(productId:Int,title: String, description: String, price: Double ,mediaList: List<Media>) {
        viewModelScope.launch {
            try {
                val ids: List<Int> = mediaList.map { it.id ?: 0 }
                _modifyState.value = UploadState.Loading
                val response = productRepository.updateProduct(
                    ModifyProductRequest(
                        productId,
                        title,
                        description,
                        price,
                        ids
                    )
                )

                if (response.isSuccessful) {
                    _modifyState.value = UploadState.Success
                    println("Product modified: ${response.body()}")
                } else {
                    _modifyState.value = UploadState.Error("Failed to modify product")
                    println("Failed to modify product: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _modifyState.value = UploadState.Error(e.message ?: "Unknown error")
                println("Error modifying product: ${e.message}")
            }
        }
    }
    /**
     * Deletes a product from the server.
     * @param productId The ID of the product to delete.
     */
    fun deleteProduct(productId:Int) {
        viewModelScope.launch {
            try {
                val response = productRepository.deleteProduct(
                    productId
                )

                if (response.isSuccessful) {
                    _deleteState.value = UploadState.Success
                    println("Product deleted: ${response.body()}")
                } else {
                    _deleteState.value = UploadState.Error("Failed to delete product")
                    println("Failed to delete product: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _deleteState.value = UploadState.Error(e.message ?: "Unknown error")
                println("Error deleting product: ${e.message}")
            }
        }
    }
    /**
     * Sends a test message to the server.
     * @param msg The message to send.
     */
    fun sendTestMsg(msg: String){
        viewModelScope.launch {
            try {
                    val response =productRepository.sendTestMsg(msg)
                    if (response.isSuccessful) {
                        println("Message sent: ${response.body()}")
                    } else {
                        println("Failed to send message: ${response.errorBody()?.string()}")
                    }
            }
            catch (e:Exception){
                _authError.value ="Network error: ${e.message}"
            }
        }
    }

}

