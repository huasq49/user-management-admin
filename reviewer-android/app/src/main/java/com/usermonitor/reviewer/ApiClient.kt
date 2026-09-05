package com.usermonitor.reviewer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
@Serializable data class Envelope<T>(val code:Int,val message:String,val data:T?=null)
@Serializable data class LoginData(val token:String)
@Serializable data class PresetPreview(val name:String="",val mode:String="",val prefixEnabled:Boolean=false,val prefixText:String="",val appendEnabled:Boolean=false,val appendText:String="",val emoticonEnabled:Boolean=false,val rules:String="",val emoticons:String="")
@Serializable data class Preset(val id:Long,val name:String,val description:String="",val uploaderName:String="",val uploaderId:String="",val fileSize:Long=0,val downloadCount:Long=0,val published:Boolean=false,val reviewStatus:String=if(published)"APPROVED" else "PENDING",val createdAt:String="")
class ApiError(message:String):Exception(message)
class ApiClient(var base:String,var token:String=""){
 private val json=Json{ignoreUnknownKeys=true;explicitNulls=false};private val http=HttpClient(OkHttp){followRedirects=false;install(io.ktor.client.plugins.HttpTimeout){requestTimeoutMillis=30_000;connectTimeoutMillis=10_000;socketTimeoutMillis=30_000};install(ContentNegotiation){json(json)}}
 private fun url(p:String)=base.trim().trimEnd('/')+p
 private suspend inline fun <reified T> read(r:HttpResponse):T {
  val text=r.bodyAsText()
  val envelope=runCatching{json.decodeFromString<Envelope<T>>(text)}.getOrElse{
   throw ApiError(if(r.status.isSuccess()) "服务器返回格式异常" else "请求失败（HTTP ${r.status.value}）")
  }
  if(!r.status.isSuccess()||envelope.code!=0)throw ApiError(envelope.message.ifBlank{"请求失败（HTTP ${r.status.value}）"})
  return envelope.data?:throw ApiError("服务器未返回数据")
 }
 suspend fun login(u:String,p:String):String{token=read<LoginData>(http.post(url("/api/reviewer/login")){contentType(ContentType.Application.Json);setBody(buildJsonObject{put("username",u);put("password",p)})}).token;return token}
 suspend fun list()=read<List<Preset>>(http.get(url("/api/reviewer/presets")){bearerAuth(token)})
 suspend fun decision(x:Preset,approved:Boolean)=read<Preset>(http.post(url("/api/reviewer/presets/${x.id}/decision")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(buildJsonObject{put("approved",approved);put("name",x.name);put("description",x.description)})})
 suspend fun preview(id:Long):PresetPreview{
  val response=http.get(url("/api/reviewer/presets/$id/preview")){bearerAuth(token)}
  val body=response.bodyAsText()
  val envelope=try{json.decodeFromString<Envelope<PresetPreview>>(body)}catch(e:kotlinx.serialization.SerializationException){
   throw ApiError(if(response.status.value==404) "服务器尚未提供在线预览接口，请更新服务端" else "预览响应格式异常（HTTP ${response.status.value}），请检查服务端版本")
  }
  if(response.status.value==404 || envelope.message.contains("NoResourceFoundException"))
   throw ApiError("服务器尚未提供在线预览接口，请更新服务端后重试")
  if(response.status.value !in 200..299 || envelope.code!=0)
   throw ApiError(envelope.message)
  return envelope.data?:throw ApiError("服务器未返回预览内容")
 }
 suspend fun download(id:Long):ByteArray{
  val r=http.get(url("/api/reviewer/presets/$id/download")){bearerAuth(token)}
  if(!r.status.isSuccess()){
   val text=r.bodyAsText();val message=runCatching{json.decodeFromString<Envelope<JsonElement>>(text).message}.getOrDefault("下载失败（HTTP ${r.status.value}）")
   throw ApiError(message)
  }
  return r.bodyAsBytes()
 }
}