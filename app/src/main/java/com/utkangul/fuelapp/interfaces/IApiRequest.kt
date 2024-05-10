package com.utkangul.fuelapp.interfaces

import com.utkangul.fuelapp.model.ApiResponseModel
import com.utkangul.fuelapp.model.PostJsonModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface IApiRequest {
    @POST("fuel-cost-calculator-523b3/us-central1/api/cost")
    fun getCost(@Body dataModal: PostJsonModel): Call<ApiResponseModel>
}