package com.saludencamino.myapplication

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import kotlinx.coroutines.runBlocking
import org.json.JSONObject


class Server {
    var url = "https://apiamanda.nevape.cl/api"

    fun login(user: String,password: String):Boolean {
        println("Antes")
        val body = JSONObject();

        body.put("correo",user);
        body.put("password",password); //"{ \"correo\" : \"$user\",\n\t\"password\" : \"$password\" }"
        var a =runBlocking {
            val (request, response, result) = Fuel.post("$url/usuario/login").jsonBody(body.toString()).awaitStringResponseResult()
            result.fold(
                { data ->
                    println(data);
                    return@fold !data.contains("fallido")
                     },
                { error -> println("An error of type ${error.exception} happened: ${error.message}") }
            )
        }

        println(a);
        return a as Boolean;
    }
    fun saveData(data: JSONObject){
        return ;

    }
}