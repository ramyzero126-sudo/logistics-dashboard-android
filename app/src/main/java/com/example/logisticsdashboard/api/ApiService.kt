package com.example.logisticsdashboard.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("city/{cityId}/riders")
    suspend fun getRiders(
        @Path("cityId") cityId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<Any>

    // ممكن نضيف باقي الـ endpoints بعدين (zones, companies, vehicle-types...)
}
