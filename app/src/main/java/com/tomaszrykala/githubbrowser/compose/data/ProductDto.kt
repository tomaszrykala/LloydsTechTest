package com.tomaszrykala.githubbrowser.compose.data

import com.squareup.moshi.Json

class ProductDto(
    @field:Json(name = "Id") val id: String,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "Description") val description: String,
    @field:Json(name = "price") val price: Long,
    @field:Json(name = "photo") val photo: String
)