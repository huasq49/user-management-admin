package com.usermonitor.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TitleUserPicker(users: List<UserItem>, loading: Boolean, close: () -> Unit, search: (String) -> Unit, select: (UserItem) -> Unit) {
    var keyword by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = close,
        title = { Text("选择授权账号") },
        confirmButton = { TextButton(close) { Text("关闭") } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(keyword, { keyword = it }, label = { Text("账号名 / 账号 ID") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row {
                    TextButton({ search(keyword.trim()) }, enabled = !loading) { Text("搜索") }
                    TextButton({ keyword = ""; search("") }, enabled = !loading) { Text("全部账号") }
                }
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (!loading && users.isEmpty()) Text("没有匹配账号，请更换关键词")
                LazyColumn(Modifier.heightIn(max = 330.dp)) {
                    items(users, key = { it.id }) { user ->
                        TextButton({ select(user) }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(user.username)
                                Text("账号 ID：${user.accountId}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    )
}
