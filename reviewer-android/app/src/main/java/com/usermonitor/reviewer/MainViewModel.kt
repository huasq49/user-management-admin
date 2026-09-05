package com.usermonitor.reviewer
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
data class UiState(val logged:Boolean=false,val loading:Boolean=false,val presets:List<Preset> = emptyList(),val notice:String?=null)
class MainViewModel(app:Application):AndroidViewModel(app){
 private val prefs=app.getSharedPreferences("reviewer",0);var server=prefs.getString("server","https://api.example.com")!!;private var api=ApiClient(server,prefs.getString("token","")!!)
 var state=androidx.compose.runtime.mutableStateOf(UiState(logged=api.token.isNotBlank()));private set
 var downloadTarget:Preset?=null
 init{if(state.value.logged)refresh()}
 fun login(u:String,p:String)=run{server=server.trim().trimEnd('/');require(server.startsWith("http://")||server.startsWith("https://")){"服务器地址必须以 http:// 或 https:// 开头"};api=ApiClient(server);val t=api.login(u.trim(),p);prefs.edit().putString("server",server).putString("token",t).apply();state.value=state.value.copy(logged=true);load()}
 fun refresh()=run{load()};private suspend fun load(){try{state.value=state.value.copy(presets=api.list())}catch(e:Exception){if(e is kotlinx.coroutines.CancellationException)throw e;if(e.message?.contains("登录已失效")==true){prefs.edit().remove("token").apply();api.token="";state.value=UiState(notice="审核员登录已失效，请重新登录");return};throw e}}
 fun decide(p:Preset,yes:Boolean)=run{api.decision(p,yes);load();state.value=state.value.copy(notice=if(yes)"已通过" else "已驳回")}
 fun logout(){prefs.edit().remove("token").apply();api.token="";downloadTarget=null;state.value=UiState()}
 fun clear(){state.value=state.value.copy(notice=null)}
 fun preview(p:Preset,on:(PresetPreview)->Unit)=run{on(api.preview(p.id))}
 private fun run(b:suspend()->Unit)=viewModelScope.launch{state.value=state.value.copy(loading=true,notice=null);try{b()}catch(e:Exception){if(e is kotlinx.coroutines.CancellationException)throw e;state.value=state.value.copy(notice=e.message?:"请求失败")}finally{state.value=state.value.copy(loading=false)}}
}