package com.saludencamino.myapplication
// To parse the JSON, install Klaxon and do:
//
//   val welcome5 = Welcome5.fromJson(jsonString)

import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Usuario (
    val codigo: String,
    val data: Data
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<Usuario>(json)
    }
}

data class Data (
    val idUsuario: Long,
    val nombre: String,
    val apellidoPaterno: String,
    val apellidoMaterno: String,
    val rut: String,
    val correo: String,
    val foto: String,
    val nacimiento: String,

    @Json(name = "empresa_idEmpresa")
    val empresaIDEmpresa: Long,

    val empresa: Empresa
)

data class Empresa (
    val idEmpresa: Long,
    val nombre: String
)
