package com.usermonitor.admin

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable data class Envelope<T>(val code:Int,val message:String,val data:T?=null,val timestamp:Long=0)
@Serializable data class LoginData(val token:String)
@Serializable data class Stats(val online:Long=0,val todayLogins:Long=0,val todayUsers:Long=0,val todayDevices:Long=0,val totalUsers:Long=0,val todayKeys:Long=0,val totalKeys:Long=0)
@Serializable data class ActivityPoint(val id:Long=0,val capturedAt:String="",val online:Long=0,val activeUsers:Long=0,val visits:Long=0)
@Serializable data class License(val id:Long,val code:String,val status:String,val durationDays:Int,val maxDevices:Int,val maxUses:Int=0,val usedCount:Int=0,val deviceBindingEnabled:Boolean=true,val deviceVerificationEnabled:Boolean=true,val activatedAt:String?=null,val expiresAt:String?=null,val remark:String?=null,val createdAt:String="")
@Serializable data class AnnouncementItem(val id:Long,val title:String,val content:String,val published:Boolean,val pinned:Boolean,val createdAt:String="")
@Serializable data class BoundDeviceItem(val id:Long,val licenseId:Long=0,val licenseCode:String="",val deviceCode:String,val deviceName:String="",val enabled:Boolean,val boundAt:String="",val lastSeenAt:String="",val lastIp:String="")
@Serializable data class MonitoredDeviceItem(val id:Long,val deviceCode:String,val deviceName:String?=null,val lastIp:String?=null,val enabled:Boolean,val online:Boolean=false,val activity:String="低",val visitCount:Long=0,val firstSeenAt:String="",val lastSeenAt:String="")
@Serializable data class VersionItem(val id:Long,val appId:String,val versionCode:Long,val versionName:String,val downloadUrl:String,val changelog:String?=null,val forceUpdate:Boolean,val published:Boolean,val createdAt:String="")
@Serializable data class RuntimeConfigItem(val accessibilityOpen:Boolean=true,val floatingWindowOpen:Boolean=true,val rootOpen:Boolean=true,val shizukuOpen:Boolean=true,val keyboardOpen:Boolean=true,val licenseValidationEnabled:Boolean=true,val presetUploadOpen:Boolean=true,val presetDownloadOpen:Boolean=true,val donateUrl:String="https://example.com/support",val afdianUserId:String="",val afdianToken:String="")
@Serializable data class UserItem(val id:Long,val accountId:String,val username:String,val avatarUrl:String="",val ipAddress:String="",val enabled:Boolean=true,val online:Boolean=false,val createdAt:String="",val lastLoginAt:String?=null)
@Serializable data class PresetPreviewItem(val name:String="",val mode:String="",val prefixEnabled:Boolean=false,val prefixText:String="",val appendEnabled:Boolean=false,val appendText:String="",val emoticonEnabled:Boolean=false,val rules:String="",val emoticons:String="")
@Serializable data class SharedPresetItem(val id:Long,val name:String,val description:String="",val uploaderName:String="",val uploaderId:String="",val fileSize:Long=0,val downloadCount:Long=0,val published:Boolean=false,val createdAt:String="")
@Serializable data class ReviewerItem(val id:Long,val username:String,val enabled:Boolean=true,val createdAt:String="",val lastLoginAt:String?=null)
@Serializable data class GeneratedReviewer(val id:Long,val username:String,val password:String,val enabled:Boolean=true)
@Serializable data class ResetUserPassword(val id:Long,val accountId:String,val username:String,val password:String)
@Serializable data class ChatMessageItem(val id:Long,val userId:Long,val accountId:String,val username:String,val content:String,val createdAt:String="",val chatBanned:Boolean=false,val chatMutedUntil:String?=null,val avatarUrl:String="",val systemRole:String="USER",val customTitle:String="",val pinned:Boolean=false)
@Serializable data class TitleApplicationItem(val id:Long,val userId:Long,val accountId:String,val username:String,val title:String,val reason:String="",val status:String="PENDING",val createdAt:String="",val reviewedAt:String?=null)
class ApiException(message:String,val unauthorized:Boolean=false):Exception(message)

class ApiClient(var baseUrl:String,var token:String="") {
 private val codec=Json{ignoreUnknownKeys=true;explicitNulls=false;encodeDefaults=true}
 private val http=HttpClient(OkHttp){followRedirects=false;install(io.ktor.client.plugins.HttpTimeout){requestTimeoutMillis=30_000;connectTimeoutMillis=10_000;socketTimeoutMillis=30_000};install(ContentNegotiation){json(codec)}}
 private fun url(path:String)=baseUrl.trim().trimEnd('/')+path
 private suspend inline fun <reified T> decode(response:HttpResponse):T{val e: Envelope<T> = response.body();if(e.code!=0)throw ApiException(e.message,e.code==401);return e.data?:throw ApiException("服务器未返回数据")}
 private suspend fun unit(response:HttpResponse){val e: Envelope<JsonElement> = response.body();if(e.code!=0)throw ApiException(e.message,e.code==401)}
 suspend fun login(user:String,password:String):String{val value=decode<LoginData>(http.post(url("/api/admin/login")){contentType(ContentType.Application.Json);setBody(buildJsonObject{put("username",user.trim());put("password",password)})}).token;token=value;return value}
 suspend fun stats()=decode<Stats>(http.get(url("/api/admin/stats")){bearerAuth(token)})
 suspend fun history()=decode<List<ActivityPoint>>(http.get(url("/api/admin/stats/history?hours=24")){bearerAuth(token)})
 suspend fun boundDevices()=decode<List<BoundDeviceItem>>(http.get(url("/api/admin/devices")){bearerAuth(token)})
 suspend fun licenses()=decode<List<License>>(http.get(url("/api/admin/licenses")){bearerAuth(token)})
 suspend fun monitoredDevices()=decode<List<MonitoredDeviceItem>>(http.get(url("/api/admin/monitored-devices")){bearerAuth(token)})
 suspend fun announcements()=decode<List<AnnouncementItem>>(http.get(url("/api/admin/announcements")){bearerAuth(token)})
 suspend fun versions()=decode<List<VersionItem>>(http.get(url("/api/admin/versions")){bearerAuth(token)})
 suspend fun runtimeConfig()=decode<RuntimeConfigItem>(http.get(url("/api/admin/runtime-config")){bearerAuth(token)})
 suspend fun users(keyword:String="",deviceCode:String="")=decode<List<UserItem>>(http.get(url("/api/admin/users?keyword=${java.net.URLEncoder.encode(keyword,"UTF-8")}&deviceCode=${java.net.URLEncoder.encode(deviceCode,"UTF-8")}")){bearerAuth(token)})
 suspend fun previewPreset(id:Long):PresetPreviewItem{
  val response=http.get(url("/api/admin/presets/$id/preview")){bearerAuth(token)}
  val body=response.bodyAsText()
  val envelope=try{codec.decodeFromString<Envelope<PresetPreviewItem>>(body)}catch(e:kotlinx.serialization.SerializationException){
   throw ApiException(if(response.status.value==404) "服务器尚未提供在线预览接口，请更新服务端" else "预览响应格式异常（HTTP ${response.status.value}），请检查服务端版本")
  }
  if(response.status.value==404 || envelope.message.contains("NoResourceFoundException"))
   throw ApiException("服务器尚未提供在线预览接口，请更新服务端后重试")
  if(response.status.value !in 200..299 || envelope.code!=0)
   throw ApiException(envelope.message,response.status.value==401 || envelope.code==401)
  return envelope.data?:throw ApiException("服务器未返回预览内容")
 }
 suspend fun presets()=decode<List<SharedPresetItem>>(http.get(url("/api/admin/presets")){bearerAuth(token)})
 suspend fun reviewers()=decode<List<ReviewerItem>>(http.get(url("/api/admin/reviewers")){bearerAuth(token)})
 suspend fun generateReviewers(count:Int)=decode<List<GeneratedReviewer>>(http.post(url("/api/admin/reviewers/generate?count=$count")){bearerAuth(token)})
suspend fun setReviewerEnabled(id:Long,enabled:Boolean)=unit(http.patch(url("/api/admin/reviewers/$id/enabled?enabled=$enabled")){bearerAuth(token)})
 suspend fun resetReviewerPassword(id:Long)=decode<GeneratedReviewer>(http.post(url("/api/admin/reviewers/$id/reset-password")){bearerAuth(token)})
 suspend fun deleteReviewer(id:Long)=unit(http.delete(url("/api/admin/reviewers/$id")){bearerAuth(token)})
 suspend fun setUserEnabled(id:Long,enabled:Boolean)=decode<UserItem>(http.patch(url("/api/admin/users/$id/enabled?enabled=$enabled")){bearerAuth(token)})
 suspend fun updateUser(id:Long,username:String)=decode<UserItem>(http.put(url("/api/admin/users/$id")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(buildJsonObject{put("username",username)})})
 suspend fun resetUserPassword(id:Long)=decode<ResetUserPassword>(http.post(url("/api/admin/users/$id/reset-password")){bearerAuth(token)})
 suspend fun logoutUser(id:Long)=unit(http.post(url("/api/admin/users/$id/logout")){bearerAuth(token)})
 suspend fun deleteUser(id:Long)=unit(http.delete(url("/api/admin/users/$id")){bearerAuth(token)})
 suspend fun updatePreset(id:Long,name:String,description:String,published:Boolean)=decode<SharedPresetItem>(http.put(url("/api/admin/presets/$id")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(buildJsonObject{put("name",name);put("description",description);put("published",published)})})
suspend fun deletePreset(id:Long)=unit(http.delete(url("/api/admin/presets/$id")){bearerAuth(token)})
 suspend fun chatMessages()=decode<List<ChatMessageItem>>(http.get(url("/api/admin/chat/messages")){bearerAuth(token)})
  suspend fun titleApplications()=decode<List<TitleApplicationItem>>(http.get(url("/api/admin/titles")){bearerAuth(token)})
  suspend fun reviewTitle(id:Long,status:String)=decode<TitleApplicationItem>(http.patch(url("/api/admin/titles/$id?status=$status")){bearerAuth(token)})
  suspend fun grantTitle(userId:Long,role:String,title:String="")=decode<JsonObject>(http.put(url("/api/admin/titles/users/$userId?role=${java.net.URLEncoder.encode(role,"UTF-8")}&title=${java.net.URLEncoder.encode(title,"UTF-8")}")){bearerAuth(token)})
 suspend fun deleteChatMessage(id:Long)=unit(http.delete(url("/api/admin/chat/messages/$id")){bearerAuth(token)})
 suspend fun clearChat()=unit(http.delete(url("/api/admin/chat/messages")){bearerAuth(token)})
 suspend fun muteChatUser(id:Long,minutes:Int)=decode<JsonObject>(http.patch(url("/api/admin/chat/users/$id/mute?minutes=$minutes")){bearerAuth(token)})
 suspend fun banChatUser(id:Long,banned:Boolean)=decode<JsonObject>(http.patch(url("/api/admin/chat/users/$id/ban?banned=$banned")){bearerAuth(token)})
 suspend fun saveRuntimeConfig(c:RuntimeConfigItem)=decode<RuntimeConfigItem>(http.put(url("/api/admin/runtime-config")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(c)})
 suspend fun generate(count:Int,days:Int,maxDevices:Int,prefix:String,remark:String,customCode:String="",maxUses:Int=0,deviceBindingEnabled:Boolean=true,deviceVerificationEnabled:Boolean=true)=decode<List<License>>(http.post(url("/api/admin/licenses/generate")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(buildJsonObject{put("count",count);put("durationDays",days);put("maxDevices",maxDevices);put("prefix",prefix);put("remark",remark);put("customCode",customCode);put("maxUses",maxUses);put("deviceBindingEnabled",deviceBindingEnabled);put("deviceVerificationEnabled",deviceVerificationEnabled)})})
 suspend fun updateLicense(id:Long,days:Int,max:Int,expires:String?,remark:String)=decode<License>(http.put(url("/api/admin/licenses/$id")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(buildJsonObject{put("durationDays",days);put("maxDevices",max);if(!expires.isNullOrBlank())put("expiresAt",expires);put("remark",remark)})})
 suspend fun rebindDevice(id:Long,code:String,name:String)=decode<BoundDeviceItem>(http.put(url("/api/admin/devices/$id/rebind")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(buildJsonObject{put("deviceCode",code);put("deviceName",name)})})
 suspend fun setLicenseStatus(id:Long,status:String)=unit(http.patch(url("/api/admin/licenses/$id/status?status=$status")){bearerAuth(token)})
 suspend fun deleteLicense(id:Long)=unit(http.delete(url("/api/admin/licenses/$id")){bearerAuth(token)})
 suspend fun setMonitoredEnabled(id:Long,enabled:Boolean)=unit(http.patch(url("/api/admin/monitored-devices/$id/enabled?enabled=$enabled")){bearerAuth(token)})
 suspend fun deleteMonitored(id:Long)=unit(http.delete(url("/api/admin/monitored-devices/$id")){bearerAuth(token)})
 suspend fun saveAnnouncement(id:Long?,title:String,content:String,published:Boolean,pinned:Boolean):AnnouncementItem{val body=buildJsonObject{put("title",title);put("content",content);put("published",published);put("pinned",pinned)};val r=if(id==null)http.post(url("/api/admin/announcements")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(body)}else http.put(url("/api/admin/announcements/$id")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(body)};return decode(r)}
 suspend fun deleteAnnouncement(id:Long)=unit(http.delete(url("/api/admin/announcements/$id")){bearerAuth(token)})
 suspend fun saveVersion(id:Long?,appId:String,code:Long,name:String,download:String,changelog:String,published:Boolean):VersionItem{val body=buildJsonObject{put("appId",appId);put("versionCode",code);put("versionName",name);put("downloadUrl",download);put("changelog",changelog);put("forceUpdate",true);put("published",published)};val r=if(id==null)http.post(url("/api/admin/versions")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(body)}else http.put(url("/api/admin/versions/$id")){bearerAuth(token);contentType(ContentType.Application.Json);setBody(body)};return decode(r)}
 suspend fun deleteVersion(id:Long)=unit(http.delete(url("/api/admin/versions/$id")){bearerAuth(token)})
}