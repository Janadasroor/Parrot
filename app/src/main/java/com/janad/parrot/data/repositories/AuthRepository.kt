package com.janad.parrot.data.repositories

import com.janad.parrot.data.api.ApiService
import com.janad.parrot.data.api.LoginReq
import com.janad.parrot.data.api.LoginRes
import com.janad.parrot.data.api.LogoutReq
import com.janad.parrot.data.api.LogoutRes
import com.janad.parrot.data.api.RefreshReq
import com.janad.parrot.data.api.RefreshRes
import com.janad.parrot.data.api.RegisterReq
import com.janad.parrot.data.api.RegisterRes
import retrofit2.Response

import javax.inject.Inject

/**
 * Repository class for handling authentication-related operations.
 *
 * This class interacts with the [ApiService] to perform login and registration requests.
 * It is injected with an instance of [ApiService] using Dagger Hilt.
 *
 * @property api The [ApiService] instance used to make network requests.
 */
class AuthRepository @Inject constructor(private val api: ApiService){

    /**
     * Performs a login request to the API.
     *
     * @param email The email of the user.
     * @param password The password of the user.
     * @return A [Response] object containing the [LoginRes] if the request is successful, or an error otherwise.
     */
    suspend fun  login(email:String, password:String): Response<LoginRes> {
       return  api.login(LoginReq(email,password))
    }
    // This function is currently unused but might be used in the future for registration functionality.
  suspend fun  register(email:String, password:String): Response<RegisterRes> {
       return  api.register(RegisterReq(email,password))
    }
    suspend fun logout(refreshToken: String): Response<LogoutRes> {
        return api.logout(LogoutReq(refreshToken))
    }
  fun refreshToken(refreshToken: String): Response<RefreshRes> {
        return api.refreshToken(RefreshReq(refreshToken))
    }
}
