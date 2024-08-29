package com.syed.soundmixer.service

import com.syed.soundmixer.models.SearchResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    @GET("search/text/?fields=id,name,previews")
    suspend fun searchSounds(@Query("query") query: String): SearchResponse

    @GET
    suspend fun searchSoundViaUrl(@Url url: String): SearchResponse

    @FormUrlEncoded
    @POST("oauth2/access_token/")
    suspend fun refreshToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): TokenResponse


    @GET("sounds/{sound_id}/download/")
    suspend fun downloadSound(
        @Path("sound_id") soundId: String,
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>
}

data class TokenResponse(
    val access_token: String,
    val refresh_token: String
)
