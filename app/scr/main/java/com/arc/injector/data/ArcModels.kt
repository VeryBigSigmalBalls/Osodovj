package com.arc.injector.data

data class Skin(val id: String, val name: String)
data class Hero(
    val id: String,
    val name: String,
    val skins: List<Skin>,
    val packages: Map<String, String>
)
