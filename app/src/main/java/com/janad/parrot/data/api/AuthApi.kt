package com.janad.parrot.data.api

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/refresh")
    fun refreshToken(@Body req: RefreshReq): retrofit2.Call<RefreshRes>}