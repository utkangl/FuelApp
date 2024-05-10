package com.utkangul.fuelappplayground.viewModel

import androidx.lifecycle.ViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.utkangul.fuelappplayground.interfaces.IApiRequest
import com.utkangul.fuelappplayground.model.ApiResponseModel
import com.utkangul.fuelappplayground.model.PostJsonModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ApiViewModel : ViewModel(){


    var apiResponse: ApiResponseModel? = null


    // using lazy to initialize value when it will be used
    private val apiService: IApiRequest by lazy {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.1.22:5000/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(IApiRequest::class.java)
    }


    fun calculateCost(dataModal: PostJsonModel){
        val call = apiService.getCost(dataModal)

        call.enqueue(object : Callback<ApiResponseModel> {
            override fun onResponse(call: Call<ApiResponseModel>, response: Response<ApiResponseModel>) {
                if (response.isSuccessful) {
                    println(response.body())
                    println("cevap geldi")
                    apiResponse = response.body()
                } else {
                    println("Error : ${response.code()}")
                    println(response)
                }
            }

            override fun onFailure(call: Call<ApiResponseModel>, t: Throwable) {
                println("Error: ${t.message}")
            }
        })
    }


}