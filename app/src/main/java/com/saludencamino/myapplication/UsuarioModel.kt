package com.saludencamino.myapplication
// To parse the JSON, install Klaxon and do:
//
//   val welcome4 = Welcome4.fromJson(jsonString)



import com.beust.klaxon.*

private val klaxon = Klaxon()

data class Usuario(
    val codigo: String,
    val data: Data
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse< Usuario>(json)
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
    val nacimiento: Any? = null,
    val telefono: Any? = null,
    val direccion: Any? = null,
    val prevision: Any? = null,
    val antecedentesEnfermedades: Any? = null,
    val morbilidades: Any? = null,
    val alergias: Any? = null,
    val cirugias: Any? = null,
    val inmunizacionRemedios: Any? = null,
    val antecedentesFamiliares: Any? = null,

    @Json(name = "counter_sms")
    val counterSMS: Any? = null,

    @Json(name = "empresa_idEmpresa")
    val empresaIDEmpresa: Long,

    val empresa: Empresa
)

data class Empresa (
    val idEmpresa: Long,
    val nombre: String,
    val logo: String
)