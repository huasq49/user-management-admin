package com.usermonitor.reviewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme(colorScheme=darkColorScheme()){App()}}}}
@Composable fun App(vm:MainViewModel=viewModel()){
 val s by vm.state;var preview by remember{mutableStateOf<PresetPreview?>(null)}
 if(!s.logged){Login(vm,s);return}
 Scaffold(topBar={TopAppBar(title={Text("预设审核台",fontWeight=FontWeight.Bold)},actions={TextButton(vm::refresh){Text("刷新")};TextButton(vm::logout){Text("退出")}})}){pad->Box(Modifier.padding(pad).fillMaxSize()){
  LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   item{Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Text("待审核与历史内容",style=MaterialTheme.typography.headlineSmall);Text("点击在线预览即可直接检查规则，无需下载文件")}}
   items(s.presets,key={it.id}){x->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(x.name,fontWeight=FontWeight.Bold);val status=when(x.reviewStatus){"APPROVED"->"已通过";"REJECTED"->"已驳回";else->"待审核"};Text(status,color=if(x.reviewStatus=="APPROVED")MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)}
    Text(x.description.ifBlank{"无简介"});Text("上传者：${x.uploaderName} (${x.uploaderId})");Text("${x.fileSize/1024} KB · ${x.createdAt}",style=MaterialTheme.typography.bodySmall)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({vm.preview(x){preview=it}},Modifier.weight(1f),enabled=!s.loading){Text("在线预览")};Button({vm.decide(x,true)},Modifier.weight(1f),enabled=!s.loading){Text("通过")};OutlinedButton({vm.decide(x,false)},Modifier.weight(1f),enabled=!s.loading){Text("驳回")}}
   }}}
  }
  if(s.loading)LinearProgressIndicator(Modifier.fillMaxWidth())
  preview?.let{v->AlertDialog(onDismissRequest={preview=null},confirmButton={TextButton({preview=null}){Text("关闭")}},title={Text(v.name)},text={Text(previewText(v))})}
  s.notice?.let{AlertDialog(vm::clear,{TextButton(vm::clear){Text("确定")}},text={Text(it)})}
 }}
}
private fun previewText(v:PresetPreview)=buildString{appendLine("模式：${v.mode}");appendLine("前缀：${if(v.prefixEnabled)v.prefixText else "关闭"}");appendLine("后缀：${if(v.appendEnabled)v.appendText else "关闭"}");appendLine();appendLine("替换规则：");appendLine(v.rules.ifBlank{"无"});appendLine();appendLine("颜文字：");append(v.emoticons.ifBlank{"无"})}
@Composable private fun Login(vm:MainViewModel,s:UiState){var server by remember{mutableStateOf(vm.server)};var user by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")};Box(Modifier.fillMaxSize().padding(24.dp),Alignment.Center){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("审核员登录",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("请使用后台生成的独立审核账号");OutlinedTextField(server,{server=it},Modifier.fillMaxWidth(),label={Text("服务器")});OutlinedTextField(user,{user=it},Modifier.fillMaxWidth(),label={Text("审核账号")});OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text("密码")},visualTransformation=PasswordVisualTransformation());Button({vm.server=server;vm.login(user,pass)},Modifier.fillMaxWidth(),enabled=!s.loading&&user.isNotBlank()&&pass.isNotBlank()){Text("登录")};s.notice?.let{Text(it,color=MaterialTheme.colorScheme.error)}}}}}