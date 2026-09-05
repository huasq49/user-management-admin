package com.usermonitor.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

data class UiState(val loggedIn:Boolean=false,val loading:Boolean=false,val error:String?=null,val message:String?=null,val stats:Stats=Stats(),val history:List<ActivityPoint> = emptyList(),val licenses:List<License> = emptyList(),val boundDevices:List<BoundDeviceItem> = emptyList(),val monitoredDevices:List<MonitoredDeviceItem> = emptyList(),val announcements:List<AnnouncementItem> = emptyList(),val versions:List<VersionItem> = emptyList(),val users:List<UserItem> = emptyList(),val presets:List<SharedPresetItem> = emptyList(),val reviewers:List<ReviewerItem> = emptyList(),val generatedCredentials:String?=null,val chatMessages:List<ChatMessageItem> = emptyList(),val titleApplications:List<TitleApplicationItem> = emptyList(),val runtimeConfig:RuntimeConfigItem=RuntimeConfigItem())
class MainViewModel(app:Application):AndroidViewModel(app){
 private val prefs=app.getSharedPreferences("admin_session",0)
 var serverUrl=prefs.getString("server_url","https://api.example.com")?:"https://api.example.com";private var api=ApiClient(serverUrl,prefs.getString("token","").orEmpty())
 var state=androidx.compose.runtime.mutableStateOf(UiState());private set
 init{if(api.token.isNotBlank()){state.value=state.value.copy(loggedIn=true);refresh()}}
 fun login(user:String,password:String)=run{api=ApiClient(serverUrl);val token=api.login(user,password);prefs.edit().putString("server_url",serverUrl).putString("token",token).apply();state.value=state.value.copy(loggedIn=true);refreshInternal()}
 fun logout(){prefs.edit().remove("token").apply();api.token="";state.value=UiState()}
 fun refresh()=run{refreshInternal()}
  fun searchUsers(keyword:String,deviceCode:String)=run{state.value=state.value.copy(users=api.users(keyword,deviceCode))}
 private suspend fun refreshInternal()=coroutineScope{val stats=async{api.stats()};val history=async{api.history()};val keys=async{api.licenses()};val bound=async{api.boundDevices()};val devices=async{api.monitoredDevices()};val notices=async{api.announcements()};val versions=async{api.versions()};val users=async{api.users()};val presets=async{api.presets()};val reviewers=async{api.reviewers()};val chat=async{api.chatMessages()};val titles=async{api.titleApplications()};val config=async{api.runtimeConfig()};state.value=state.value.copy(stats=stats.await(),history=history.await(),licenses=keys.await(),boundDevices=bound.await(),monitoredDevices=devices.await(),announcements=notices.await(),versions=versions.await(),users=users.await(),presets=presets.await(),reviewers=reviewers.await(),chatMessages=chat.await(),titleApplications=titles.await(),runtimeConfig=config.await())}
 fun generate(c:Int,d:Int,m:Int,p:String,r:String,custom:String="")=run{api.generate(c,d,m,p,r,custom);refreshInternal();notify("卡密生成成功")}
 fun updateLicense(k:License,days:Int,max:Int,expires:String?,remark:String)=run{api.updateLicense(k.id,days,max,expires,remark);refreshInternal();notify("卡密信息已更新")}
 fun rebindDevice(d:BoundDeviceItem,code:String,name:String)=run{api.rebindDevice(d.id,code,name);refreshInternal();notify("设备码已更换")}
 fun toggleLicense(k:License)=run{api.setLicenseStatus(k.id,if(k.status=="DISABLED")"ACTIVE" else "DISABLED");refreshInternal()}
 fun deleteLicense(id:Long)=run{api.deleteLicense(id);refreshInternal();notify("卡密已删除")}
 fun toggleDevice(d:MonitoredDeviceItem)=run{api.setMonitoredEnabled(d.id,!d.enabled);refreshInternal()}
 fun deleteDevice(id:Long)=run{api.deleteMonitored(id);refreshInternal()}
 fun saveAnnouncement(id:Long?,t:String,c:String,p:Boolean,pin:Boolean)=run{api.saveAnnouncement(id,t,c,p,pin);refreshInternal();notify("公告已保存")}
 fun deleteAnnouncement(id:Long)=run{api.deleteAnnouncement(id);refreshInternal()}
 fun saveRuntimeConfig(c:RuntimeConfigItem)=run{api.saveRuntimeConfig(c);refreshInternal();notify("云端运行配置已保存")}
 fun saveVersion(id:Long?,appId:String,code:Long,name:String,url:String,log:String,published:Boolean)=run{api.saveVersion(id,appId,code,name,url,log,published);refreshInternal();notify(if(id==null)"强制更新版本已发布" else "版本配置已更新")}
 fun deleteVersion(id:Long)=run{api.deleteVersion(id);refreshInternal()}
 fun toggleUser(u:UserItem)=run{api.setUserEnabled(u.id,!u.enabled);refreshInternal();notify(if(u.enabled)"用户已禁用" else "用户已启用")}
 fun generateReviewers(count:Int)=run{val x=api.generateReviewers(count);refreshInternal();state.value=state.value.copy(generatedCredentials=x.joinToString("\n"){"${it.username}  ${it.password}"})}
 fun toggleReviewer(r:ReviewerItem)=run{api.setReviewerEnabled(r.id,!r.enabled);refreshInternal();notify(if(r.enabled)"审核员已禁用" else "审核员已启用")}
 fun resetReviewer(r:ReviewerItem)=run{val x=api.resetReviewerPassword(r.id);refreshInternal();state.value=state.value.copy(generatedCredentials="${x.username}  ${x.password}")}
 fun deleteReviewer(id:Long)=run{api.deleteReviewer(id);refreshInternal();notify("审核员已删除")}
 fun saveUser(u:UserItem,n:String,c:String,d:String)=run{api.updateUser(u.id,n);refreshInternal();notify("用户资料已更新")}
 fun resetUser(u:UserItem)=run{val x=api.resetUserPassword(u.id);refreshInternal();state.value=state.value.copy(generatedCredentials="${x.username}  ${x.password}")}
 fun logoutUser(u:UserItem)=run{api.logoutUser(u.id);notify("用户已强制下线")}
 fun deleteUser(id:Long)=run{api.deleteUser(id);refreshInternal();notify("用户及其预设已删除")}
 fun clearCredentials(){state.value=state.value.copy(generatedCredentials=null)}
 fun previewPreset(p:SharedPresetItem,on:(PresetPreviewItem)->Unit)=run{on(api.previewPreset(p.id))}
 fun savePreset(p:SharedPresetItem,name:String,description:String,published:Boolean)=run{api.updatePreset(p.id,name,description,published);refreshInternal();notify("在线预设已更新")}
 fun deletePreset(id:Long)=run{api.deletePreset(id);refreshInternal();notify("在线预设已删除")}
 fun deleteChatMessage(id:Long)=run{api.deleteChatMessage(id);refreshInternal()}
 fun clearChat()=run{api.clearChat();refreshInternal();notify("聊天记录已清空")}
 fun muteChatUser(id:Long,minutes:Int)=run{api.muteChatUser(id,minutes);refreshInternal();notify(if(minutes<=0)"已解除禁言" else "已禁言 $minutes 分钟")}
 fun banChatUser(id:Long,banned:Boolean)=run{api.banChatUser(id,banned);refreshInternal();notify(if(banned)"已封禁聊天" else "已解除聊天封禁")}
 fun reviewTitle(id:Long,status:String)=run{api.reviewTitle(id,status);refreshInternal();notify("头衔申请已处理")}
  fun grantTitle(userId:Long,role:String,title:String="")=run{api.grantTitle(userId,role,title);refreshInternal();notify("头衔授权已更新")}
  fun clearNotice(){state.value=state.value.copy(error=null,message=null)}
 private fun notify(text:String){state.value=state.value.copy(message=text)}
 private fun run(block:suspend()->Unit)=viewModelScope.launch{state.value=state.value.copy(loading=true,error=null,message=null);try{block()}catch(e:Exception){if(e is kotlinx.coroutines.CancellationException)throw e;if(e is ApiException&&e.unauthorized){prefs.edit().remove("token").apply();state.value=UiState(error="登录已过期，请重新登录")}else state.value=state.value.copy(error=e.message?:"请求失败")}finally{state.value=state.value.copy(loading=false)}}
}