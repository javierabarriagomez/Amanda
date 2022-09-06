package com.saludencamino.myapplication

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import android.content.SharedPreferences
import com.beust.klaxon.*

private val klaxon = Klaxon()

class Server {
    var url = "https://apiamanda.nevape.cl/api" //Nevape
    //var url = "http://desarrollo.saludencamino.com:3211/api"
    fun login(user: String,password: String,context: Context):Boolean {
        println("Antes")
        val body = JSONObject();

        body.put("correo",user);
        body.put("password",password); //"{ \"correo\" : \"$user\",\n\t\"password\" : \"$password\" }"
        var a =runBlocking {
            val (request, response, result) = Fuel.post("$url/usuario/login").jsonBody(body.toString()).awaitStringResponseResult()
            result.fold(
                { data ->


                    if(klaxon.toJsonString(data).contains("login fallido")){
                        return@fold false
                    }
                    val usuario = Usuario.fromJson(data);

                    if (usuario != null) {
                        println(usuario.data.idUsuario)
                        println(usuario.data.nombre)

                        val prefs = context.getSharedPreferences(
                            "com.saludencamino.myapplication", Context.MODE_PRIVATE
                        )
                        prefs.edit().putBoolean("sesion_iniciada",true).apply();
                        prefs.edit().putLong("idUsuario",usuario.data.idUsuario).apply()
                        prefs.edit().putString("FotoUsuario",usuario.data.foto).apply()
                        prefs.edit().putString("nombreUsuario",usuario.data.nombre).apply()

                        return@fold !data.contains("fallido")
                    }else{
                        return@fold false;
                    }

                     },
                { error -> println("An error of type ${error.exception} happened: ${error.message}") }
            )
        }

        println(a);
        return a as Boolean;
    }
    fun saveData(context: Context, data: JSONObject):Boolean{
        println(data);
        var a =runBlocking {
            val (request, response, result) = Fuel.post("$url/medicionUsuario/createMedicionesWithIdUsuario").jsonBody(data.toString()).awaitStringResponseResult()
            result.fold(
                { data ->
                    println(data);
                    return@fold data.contains("ok")
                },
                { error -> println("An error of type ${error.exception} happened: ${error.message}") }
            )
        }

        println(a);
        return a as Boolean;

    }

}