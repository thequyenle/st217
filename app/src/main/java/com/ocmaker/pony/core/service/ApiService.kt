package com.ocmaker.pony.core.service
import com.ocmaker.pony.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("/api/ST193_PixelMaker")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}