from pathlib import Path
import re,json,hashlib
ROOT=Path(__file__).resolve().parents[1]/'backend'
SRC=ROOT/'src/main/java/com/usermonitor/server'
OUT=ROOT/'src/main/resources/static'
S={'type':'string'};I={'type':'integer','format':'int64'};B={'type':'boolean'}
def obj(fields):
 return {'type':'object','properties':{k:({'type':'boolean'} if k in ['enabled','online','published','publicVisible','pinned','forceUpdate','valid','changed','accepted','prefixEnabled','appendEnabled','emoticonEnabled','chatBanned','deviceBindingEnabled','deviceVerificationEnabled','bypass'] or k.endswith('Open') else I.copy() if k in ['id','userId','deviceId','licenseId','versionCode','fileSize','downloadCount','count','onlineCount','visitCount','durationDays','maxDevices','maxUses','usedCount','online','todayLogins','todayUsers','todayDevices','totalUsers','todayKeys','totalKeys','activeUsers','visits'] else S.copy()) for k in fields.split()}}
schemas={}
for p in (SRC/'model').glob('*.java'):
 text=p.read_text(); props={}
 for m in re.finditer(r'private\s+(?!static|final)([\w<>\[\].]+)\s+(\w+)\s*(?:=[^;]*)?;',text):
  typ,name=m.groups(); pre=text[max(0,text.rfind(';',0,m.start())+1):m.start()]
  if 'JsonIgnore' in pre:continue
  props[name]=B.copy() if typ in ['boolean','Boolean'] else I.copy() if typ in ['long','Long','Integer','int'] else {'type':'string','description':'服务器本地日期时间，ISO-8601，不携带时区偏移','example':'2026-09-05T12:00:00'} if typ=='LocalDateTime' else {'type':'string','format':'byte'} if typ=='byte[]' else S.copy() if typ in ['String','Status'] else {'$ref':'#/components/schemas/'+typ}
 schemas[p.stem]={'type':'object','properties':props}
schemas.update({
  'RoleGrant':obj('id accountId username systemRole customTitle'),
 'UserProfile':obj('id accountId username avatarUrl createdAt systemRole customTitle'),
 'UserLogin':obj('id accountId username avatarUrl createdAt systemRole customTitle token expiresAt'),
 'AdminUser':obj('id accountId username avatarUrl createdAt systemRole customTitle ipAddress enabled online lastLoginAt'),
 'PresetSummary':obj('id name description uploaderName uploaderId fileSize downloadCount published createdAt'),
 'PresetClient':obj('id name description fileSize downloadCount published publicVisible createdAt updatedAt'),
 'PresetPreview':obj('name mode prefixEnabled prefixText appendEnabled appendText emoticonEnabled rules emoticons'),
 'Reviewer':obj('id username enabled createdAt lastLoginAt'),
 'Credentials':obj('id accountId username password enabled'),
 'ChatItem':obj('id content createdAt userId accountId username avatarUrl ipRegion ipAddress systemRole customTitle pinned'),
 'AdminChatItem':obj('id content createdAt userId accountId username chatBanned chatMutedUntil'),
 'TitleItem':obj('id userId accountId username title reason status createdAt reviewedAt'),
 'BoundDevice':obj('id licenseId licenseCode deviceCode deviceName enabled boundAt lastSeenAt lastIp'),
 'Stats':{'type':'object','properties':{k:I.copy() for k in 'online todayLogins todayUsers todayDevices totalUsers todayKeys totalKeys'.split()}},
 'Empty':{'type':'object','nullable':True}, 'Token':obj('token'),
 'LicenseSession':obj('token expiresAt licenseExpiresAt deviceId bypass'),
 'HeartbeatResult':obj('valid serverTime licenseExpiresAt'),
 'SponsorResult':{'type':'object','properties':{'items':{'type':'array','items':obj('name amount plan')},'count':I.copy()}},
 'DeviceState':obj('enabled'),'CardState':obj('valid'),'PingResult':obj('accepted'),'Changed':obj('changed'),
 'PresetApiState':obj('uploadOpen downloadOpen')})
schemas['MyTitle']=obj('id title reason status createdAt')
schemas['MonitoredDevice']=obj('id deviceCode deviceName lastIp enabled online activity visitCount firstSeenAt lastSeenAt')
schemas['LicenseSession']['properties']['deviceId']=I.copy()
schemas['AdminChatUser']=obj('id chatBanned chatMutedUntil')
schemas['PresetClient']['properties']['uploader']={'$ref':'#/components/schemas/UserProfile'}
# Typed request records and Bean Validation constraints.
def split_params(s):
 result=[];level=0;quote=False;start=0
 for i,c in enumerate(s):
  if c=='"' and (i==0 or s[i-1]!='\\'):quote=not quote
  if not quote:
   if c in '(<':level+=1
   elif c in ')>':level-=1
   elif c==',' and level==0:result.append(s[start:i]);start=i+1
 result.append(s[start:]);return result
for p in [SRC/'dto/Requests.java',SRC/'controller/MonitorController.java']:
 text=p.read_text()
 for m in re.finditer(r'record\s+(\w+)\(',text):
  start=m.end();i=start;depth=1
  while depth:
   if text[i]=='(':depth+=1
   if text[i]==')':depth-=1
   i+=1
  props={};req=[]
  for part in split_params(text[start:i-1]):
   match=re.search(r'(String|int|long|Integer|Boolean|boolean|LocalDateTime)\s+(\w+)\s*$',part.strip())
   if not match:raise ValueError(part)
   typ,key=match.groups();spec=B.copy() if typ in ['boolean','Boolean'] else I.copy() if typ in ['int','long','Integer'] else S.copy()
   for ann,prop in [('Min','minimum'),('Max','maximum')]:
    v=re.search('@'+ann+r'\((\d+)\)',part)
    if v:spec[prop]=int(v[1])
   v=re.search(r'@Size\(max=(\d+)\)',part)
   if v:spec['maxLength']=int(v[1])
   if '@NotBlank' in part:spec['minLength']=1;req.append(key)
   if typ in ['int','long']:req.append(key)
   if typ=='LocalDateTime':spec['description']='本地 ISO 日期时间，如 2026-09-05T12:00:00；null 不修改'
   props[key]=spec
  schemas[m[1]]={'type':'object','properties':props,**({'required':req} if req else {})}
# Each controller operation is explicitly described; coverage is checked against source below.
entries=[]
def add(method,path,title,response='Empty',body=None,query='',note='',auth=None):
 entries.append(dict(method=method,path=path,title=title,response=response,body=body,query=query,note=note,auth=auth))
A='/api/admin'; C='/api/client'; R='/api/reviewer'
add('POST',A+'/login','管理员登录','Token','AdminLogin',auth='public')
add('POST',A+'/logout','注销管理员会话')
add('GET',A+'/stats','业务统计','Stats',note='todayUsers/totalUsers 实际统计监控设备，不是注册账号；online 为两分钟内活跃且启用设备。')
add('GET',A+'/stats/history','活跃历史','ActivitySnapshot[]',query='hours:int=24',note='采样记录按服务实现返回。')
add('GET',A+'/licenses','全部卡密','LicenseKey[]')
add('POST',A+'/licenses/generate','生成卡密','LicenseKey[]','GenerateKeys',note='count 1–1000；自定义 customCode 仅允许 count=1；maxUses=0 不限；缺省设备开关为 true。')
add('PUT',A+'/licenses/{id}','修改卡密','LicenseKey','LicenseUpdate',note='仅更新非 null 字段；expiresAt 同步调整卡密 ACTIVE/EXPIRED 状态。')
add('PATCH',A+'/licenses/{id}/status','启停卡密',query='status:string!',note='status：UNUSED / ACTIVE / DISABLED / EXPIRED。')
add('GET',A+'/licenses/{id}/devices','卡密绑定设备','BoundDevice[]')
add('DELETE',A+'/licenses/{id}','删除卡密',note='不可恢复；关联外键可能使删除失败，应先处理绑定关系。')
add('GET',A+'/devices','全部绑定设备','BoundDevice[]')
add('PATCH',A+'/devices/{id}/enabled','启停绑定设备',query='enabled:bool!')
add('PUT',A+'/devices/{id}/rebind','重绑设备','Device','DeviceRebind',note='重新绑定后启用该设备；同一卡密下设备码不能重复。')
add('DELETE',A+'/devices/{id}','解绑设备',note='不可恢复，关联会话可能受影响。')
add('GET',A+'/monitored-devices','设备访问列表','MonitoredDevice[]',note='额外返回 online、activity：在线为两分钟内访问；activity 为高/中/低。')
add('PATCH',A+'/monitored-devices/{id}/enabled','设备码授权开关',query='enabled:bool!')
add('DELETE',A+'/monitored-devices/{id}','删除设备访问记录')
add('GET',A+'/users','用户列表与搜索','AdminUser[]',query='keyword:string=;deviceCode:string=',note='deviceCode 非空优先；keyword 匹配用户名/账号ID；当前新用户不绑定设备码，设备码查询主要兼容旧数据。')
add('PATCH',A+'/users/{id}/enabled','启停用户','AdminUser',query='enabled:bool!',note='撤销该用户全部会话。')
add('PUT',A+'/users/{id}','编辑用户','AdminUser',{'username':'示例用户','enabled':True},note='可选字段 username、enabled；用户名2–20位文字/字母/数字/下划线/横线；忽略设备字段；撤销全部会话。')
add('POST',A+'/users/{id}/reset-password','重置用户密码','Credentials',note='返回一次性明文 password；撤销全部会话，勿记录到日志。')
add('POST',A+'/users/{id}/logout','强制用户下线')
add('DELETE',A+'/users/{id}','删除用户','Empty',note='删除用户、其会话、预设和聊天；不可恢复，其他关联可能阻止删除。')
add('GET',A+'/presets','全部在线预设','PresetSummary[]')
add('GET',A+'/presets/{id}/preview','解密预览预设','PresetPreview',note='支持未发布/私有；MND1 AES-GCM 校验；损坏返回400；不返回原文件、不增加下载次数。')
add('PUT',A+'/presets/{id}','编辑和审核预设','PresetSummary',{'name':'示例预设','description':'简介','published':True},note='字段均可选；published 控制审核发布，客户端公开可见还须 publicVisible=true；不修改加密文件内部名称。')
add('DELETE',A+'/presets/{id}','删除在线预设')
add('GET',A+'/reviewers','审核员列表','Reviewer[]')
add('POST',A+'/reviewers/generate','生成审核员','Credentials[]',query='count:int=1',note='count 1–100；明文密码仅本次返回。')
add('PATCH',A+'/reviewers/{id}/enabled','启停审核员',query='enabled:bool!')
add('POST',A+'/reviewers/{id}/reset-password','重置审核员密码','Credentials',note='撤销旧会话；返回一次性明文 password。')
add('DELETE',A+'/reviewers/{id}','删除审核员及会话')
add('GET',A+'/chat/messages','管理聊天消息','AdminChatItem[]',note='最近300条，ID倒序。')
add('DELETE',A+'/chat/messages/{id}','删除单条消息')
add('DELETE',A+'/chat/messages','清空所有聊天消息',note='全量删除，不可恢复。')
add('PATCH',A+'/chat/users/{id}/mute','禁言或解除','UserAccount',query='minutes:int!',note='minutes<=0 解除，最大43200分钟；返回用户简要状态，不是完整用户实体。')
add('PATCH',A+'/chat/users/{id}/ban','封禁或解除聊天','UserAccount',query='banned:bool!',note='返回用户简要状态。')
add('GET',A+'/titles','头衔申请列表','TitleItem[]')
add('PUT',A+'/titles/users/{id}','授予用户角色头衔','RoleGrant',query='role:string!;title:string=',note='role=USER/AUTHOR/SYSTEM_ADMIN；此为用户社区角色，不是后台登录权限。title留空时作者/管理员使用默认头衔。')
add('PATCH',A+'/titles/{id}','审核头衔申请','TitleItem',query='status:string!',note='status=APPROVED/REJECTED；不能设为PENDING；通过后写入用户头衔。')
for noun,model,req,title in [('announcements','Announcement','AnnouncementSave','公告'),('versions','AppVersion','VersionSave','版本')]:
 add('GET',A+'/'+noun,title+'列表',model+'[]');add('POST',A+'/'+noun,'新建'+title,model,req);add('PUT',A+'/'+noun+'/{id}','编辑'+title,model,req);add('DELETE',A+'/'+noun+'/{id}','删除'+title)
add('GET',A+'/runtime-config','云端配置','RuntimeConfig',note='afdianToken 不回显。')
add('PUT',A+'/runtime-config','更新云端配置','RuntimeConfig','RuntimeConfigSave',note='字段均可选；null不更新；afdianToken/ donateUrl 空白不更新；afdianUserId 空白可清空。')
add('POST',R+'/login','审核员登录','Token',{'username':'审核员账号','password':'<PASSWORD>'},auth='public')
add('GET',R+'/presets','待审核与历史预设','PresetSummary[]')
add('GET',R+'/presets/{id}/preview','审核员在线预览','PresetPreview',note='未发布/私有也可预览；不计下载。')
add('GET',R+'/presets/{id}/download','兼容旧版审核下载','binary',note='返回原始MND；新接入优先用preview，不增加下载次数。')
add('POST',R+'/presets/{id}/decision','审核决定','PresetSummary',{'approved':True,'name':'示例预设','description':'简介'},note='approved 必须显式传递，否则默认false；name/description可选。当前源码只保存published，无独立驳回枚举。')
add('POST',C+'/users/register','注册用户','UserProfile','multipart:username! password! confirmPassword! deviceCode deviceName avatar@',auth='public',note='账号2–20位文字/字母/数字/_/-；密码6–64位且两次相同；头像PNG/JPEG、边长≤4096、≤200KB；设备字段兼容但不绑定。注册不自动登录。')
add('POST',C+'/users/login','用户登录','UserLogin',{'login':'用户名或账号ID','password':'<PASSWORD>','deviceCode':''},auth='public',note='撤销旧用户会话；新会话3天有效；deviceCode可省略。')
add('GET',C+'/users/me','当前用户','UserProfile')
add('PATCH',C+'/users/me/name','修改名字','UserProfile',{'username':'新名字'})
add('PATCH',C+'/users/me/password','修改密码','Changed',{'oldPassword':'<OLD>','newPassword':'<NEW>','confirmPassword':'<NEW>'})
add('POST',C+'/users/me/avatar','更换头像','UserProfile','multipart:file@!',note='file不能为空，≤200KB。')
add('GET',C+'/users/{id}/avatar','获取头像','image',auth='public',note='id 为账号ID或用户名，不是内部数字主键；无头像返回404；缓存7天。')
add('GET',C+'/users/titles','我的头衔申请','MyTitle[]')
add('POST',C+'/users/titles','申请自定义头衔','MyTitle',{'title':'示例头衔','reason':'申请理由'},note='title为2–32个非空白字符，禁止申请作者/管理员身份名；reason可选。')
add('GET',C+'/presets','公开预设','PresetClient[]',auth='public',note='仅published=true 且 publicVisible=true。')
add('GET',C+'/presets/mine','我的全部上传','PresetClient[]')
add('POST',C+'/presets','上传预设','PresetClient','multipart:name! description publicVisible file@!',note='上传开关需开放；name 1–80字；description默认空，publicVisible默认true；.mnd文件1B–2MB。上传仅校验扩展名和大小，解密有效性由预览检查。')
add('PATCH',C+'/presets/mine/{id}/visibility','修改公开范围','PresetClient',query='publicVisible:bool!',note='只能本人上传；公开仍需通过审核。')
add('GET',C+'/presets/mine/{id}/download','下载自己的预设','binary',note='下载开关需开放；只能本人；不增加公开下载次数。')
add('GET',C+'/presets/{id}','公开预设元信息','PresetClient',auth='public',note='仅数字ID；这不是解密预览接口。')
add('GET',C+'/presets/{id}/download','下载公开预设','binary',auth='public',note='下载开关需开放；必须审核通过且公开；每次成功增加下载次数。')
add('GET',C+'/chat/messages','用户聊天消息','ChatItem[]',query='after:int=0',note='after<=0 最近100条并按ID升序；after>0 返回ID更大的最多200条。')
add('POST',C+'/chat/messages','发送消息','ChatItem',{'content':'你好'},note='非空，trim后最多500字符；禁言/封禁不能发送。成功广播/topic/chat。')
add('PATCH',C+'/chat/messages/{id}/pin','置顶聊天消息','ChatItem',query='pinned:bool!',note='用户角色仅AUTHOR/SYSTEM_ADMIN允许，普通用户拒绝；成功广播/topic/chat。')
for path,title,req,res,note in [('device-status','登记并查询设备授权','DeviceStatus','DeviceState','新设备默认未启用；更新最后访问和IP。'),('validate-card','验证卡密','ValidateCard','CardState','有副作用：成功增加使用次数、首次激活计算到期；依卡密配置要求云端设备启用。'),('login','卡密设备会话登录','ClientLogin','LicenseSession','区别于用户登录；返回卡密会话token，默认有效期由server配置决定。'),('heartbeat','卡密会话心跳','Heartbeat','HeartbeatResult','JSON body携带卡密token，不是Authorization用户令牌。'),('monitor','设备活动上报','Ping','PingResult','visit=true增加访问次数；需每次生成新签名nonce。')]:add('POST',C+'/'+path,title,res,req,note=note,auth='signature')
add('GET',C+'/announcements','公开公告','Announcement[]',auth='public')
add('GET',C+'/preset-api-status','上传下载开关','PresetApiState',auth='signature')
add('GET',C+'/runtime-config','运行模式配置','RuntimeConfig',auth='signature')
add('GET',C+'/version','最新发布版本','AppVersion',query='appId:string!;versionCode:int!',auth='public',note='返回指定appId最高已发布版本或null；服务端未比较入参versionCode，客户端自行比较。')
add('GET',C+'/sponsors','赞助名单','SponsorResult',auth='signature',note='爱发电需服务端配置；第三方不可用/未配置时可能返回400。')
add('GET','/api/overview','兼容旧概览','Stats',auth='admin',note='要求管理员令牌；建议新接入使用/api/admin/stats。')
add('GET','/api/dashboard','兼容旧历史','ActivitySnapshot[]',query='hours:int=24',auth='admin',note='要求管理员令牌；新接入使用/api/admin/stats/history。')
# Verify all declared HTTP mappings are present; normalize regex path constraints.
actual=set()
for p in (SRC/'controller').glob('*.java'):
 s=p.read_text();base=re.search(r'@RequestMapping\("([^"]+)"\)',s);base=base[1] if base else ''
 for m in re.finditer(r'@(Get|Post|Put|Patch|Delete)Mapping(?:\(([^\n]*?)\))?',s):
  arg=m[2] or '';path=re.search(r'"([^"]*)"',arg);path=path[1] if path else ''
  path=re.sub(r'\{(\w+):[^}]+\}',r'{\1}',path);actual.add((m[1].upper(),base+path))
documented={(e['method'],e['path']) for e in entries}
assert actual==documented, {'undocumented':actual-documented,'extra':documented-actual}
security={'admin':{'type':'http','scheme':'bearer','description':'管理员会话token，仅由/api/admin/login获得'},'reviewer':{'type':'http','scheme':'bearer','description':'审核员会话token，12小时有效'},'user':{'type':'http','scheme':'bearer','description':'普通用户会话token，3天有效'},'timestamp':{'type':'apiKey','in':'header','name':'X-Timestamp'},'nonce':{'type':'apiKey','in':'header','name':'X-Nonce'},'signature':{'type':'apiKey','in':'header','name':'X-Signature'}}
paths={}
def ref(name):
 if name.endswith('[]'):return {'type':'array','items':ref(name[:-2])}
 assert name in schemas,name
 return {'$ref':'#/components/schemas/'+name}
def infer(v):return {'type':'boolean','example':v} if isinstance(v,bool) else {'type':'integer','example':v} if isinstance(v,int) else {'type':'string','example':v}
for e in entries:
 auth=e['auth'] or ('admin' if e['path'].startswith(A) else 'reviewer' if e['path'].startswith(R) else 'user')
 group={'admin':'管理员','reviewer':'审核员','signature':'客户端签名','user':'用户账号','public':'公开/兼容'}[auth];e['auth']=auth;e['group']=group
 params=[{'name':k,'in':'path','required':True,'schema':S.copy() if e['response']=='image' else I.copy(),'description':'路径ID；除头像说明外均为数据库内部数字主键'} for k in re.findall(r'\{(\w+)\}',e['path'])]
 for q in filter(None,e['query'].split(';')):
  name,typ=q.split(':',1);req=typ.endswith('!');default=typ.split('=',1)[1] if '=' in typ else None;typ=typ.rstrip('!').split('=')[0];spec={'int':I,'bool':B,'string':S}[typ].copy()
  if default is not None:spec['default']=int(default) if typ=='int' else default
  params.append({'name':name,'in':'query','required':req,'schema':spec})
 body=e['body'];request=None
 if isinstance(body,str) and body.startswith('multipart:'):
  props={};req=[]
  for f in body[10:].split():
   name=f.replace('@','').replace('!','');props[name]={'type':'string','format':'binary'} if '@' in f else B.copy() if name=='publicVisible' else S.copy()
   if '!' in f:req.append(name)
  request={'required':True,'content':{'multipart/form-data':{'schema':{'type':'object','properties':props,'required':req}}}}
 elif body:
  bs=ref(body) if isinstance(body,str) else {'type':'object','properties':{k:infer(v) for k,v in body.items()}}
  request={'required':True,'content':{'application/json':{'schema':bs}}}
 result=e['response'];schema=ref(result) if result not in ['binary','image'] else {'type':'string','format':'binary'}
 if result not in ['binary','image']:schema={'type':'object','required':['code','message','timestamp'],'properties':{'code':{'type':'integer','example':0},'message':{'type':'string','example':'ok'},'timestamp':I.copy(),'data':schema},'description':'data 可为 null；客户端必须先检查 code 和 HTTP 状态。'}
 op={'summary':e['title'],'operationId':e['method'].lower()+re.sub(r'\W+','_',e['path']),'tags':[group],'description':e['note'] or '参数按下方定义传递；成功结果位于统一响应 data。','security':[] if auth=='public' else [{'timestamp':[],'nonce':[],'signature':[]}] if auth=='signature' else [{auth:[]}],'parameters':params,'responses':{'200':{'description':'成功；data定义如下','content':{('application/octet-stream' if result=='binary' else 'image/*' if result=='image' else 'application/json'):{'schema':schema}}},**{str(c):{'description':d,'content':{'application/json':{'schema':{'$ref':'#/components/schemas/Error'}}}} for c,d in [(400,'参数/业务错误或预设损坏'),(401,'签名/身份无效或权限拒绝'),(404,'接口不存在或头像不存在'),(500,'内部错误；部分旧接口缺少必填请求头可能返回500')]}}}
 if request:op['requestBody']=request
 paths.setdefault(e['path'],{})[e['method'].lower()]=op
schemas['Error']={'type':'object','properties':{'code':{'type':'integer'},'message':S,'data':{'nullable':True},'timestamp':I}}
# Validate refs and document actual response deviations.
schemas['AdminChatUser']=obj('id chatBanned chatMutedUntil')
for path in [A+'/chat/users/{id}/mute',A+'/chat/users/{id}/ban']:
 paths[path]['patch']['responses']['200']['content']['application/json']['schema']['properties']['data']=ref('AdminChatUser')
spec={'openapi':'3.0.3','info':{'title':'User Monitor · 全量 API 对接文档','version':'1.0.0-preview','description':'以当前源码合并后端源代码为准，覆盖全部HTTP路由；不会自动调用写接口。时间字段多为服务器本地ISO时间，无时区偏移。鉴权token分管理员、审核员、用户、卡密会话四类，不能混用。'},'servers':[{'url':'/','description':'当前 WebUI 同源服务器'}],'tags':[{'name':n} for n in ['管理员','审核员','用户账号','客户端签名','公开/兼容']],'paths':paths,'components':{'securitySchemes':security,'schemas':schemas},'x-route-count':len(entries)}
# Human-readable field descriptions and precise examples.
labels=dict(line.split('=',1) for line in """id=数据库内部数字主键，不是账号ID
accountId=九位字符串账号ID；不能用作管理接口的数字主键
username=用户名 / 登录账号
password=密码；仅发送到受信任服务器，不写日志
confirmPassword=确认密码，必须与新密码一致
oldPassword=当前密码
newPassword=新密码，6–64位
login=用户名或九位账号ID
token=对应身份的会话令牌；不同登录接口令牌不能混用
expiresAt=会话或授权到期时间
licenseExpiresAt=卡密到期时间
createdAt=创建时间
updatedAt=更新时间
capturedAt=统计采样时间
lastLoginAt=最后登录时间，可能为null
lastSeenAt=最后活跃时间，可能为null
firstSeenAt=首次访问时间
boundAt=绑定时间
activatedAt=首次激活时间
reviewedAt=审核时间，未审核为null
deviceCode=设备唯一标识；请保持稳定
deviceName=设备显示名称
licenseKey=客户端提交的卡密字符串
code=卡密内容
status=状态值，具体枚举见接口说明
count=生成数量或结果数量
durationDays=首次激活后的有效天数，最小1
maxDevices=启用绑定设备数量上限，1–20
maxUses=卡密验证次数上限，0表示不限
usedCount=已使用的卡密验证次数
prefix=生成卡密前缀
customCode=自定义卡密，仅允许单码创建，最长64字
remark=备注
deviceBindingEnabled=是否启用设备绑定规则
deviceVerificationEnabled=validate-card是否检查云端设备授权
enabled=是否启用 / 授权
online=当前在线状态；统计对象中同名字段为在线数量
activity=访问活跃度：高 / 中 / 低
visit=本次上报是否计入访问次数
visitCount=累计访问次数
visits=累计访问量采样
activeUsers=活跃设备数量采样
todayLogins=今日卡密会话登录次数
todayUsers=今日活跃监控设备数，不是注册用户数
todayDevices=今日新增监控设备数
totalUsers=监控设备总数，不是注册用户总数
todayKeys=今日生成卡密数
totalKeys=卡密总数
lastIp=最后来源IP
ipAddress=管理用户接口为IP；客户端聊天为地区文本，不是原始IP
ipRegion=IP地区文本
avatarUrl=相对服务器根地址的头像URL，无头像为空
systemRole=社区角色：USER / AUTHOR / SYSTEM_ADMIN，不代表后台登录权限
customTitle=自定义头衔，可能为空
chatBanned=是否禁止公共聊天
chatMutedUntil=禁言截止时间，null表示未禁言
doNotDisturb=实体中的免打扰标记；当前源码没有修改此值的HTTP路由
name=名称
content=内容；聊天文本trim后1–500字符
description=简介
published=是否发布 / 审核通过
pinned=是否置顶
publicVisible=上传者是否允许公开；还需published=true才进入公开列表
fileSize=文件大小，单位字节
downloadCount=公开下载累计次数
uploader=上传用户资料
uploaderName=上传用户名
uploaderId=上传者九位字符串账号ID
userId=关联用户内部数字主键
licenseId=关联卡密内部数字主键
licenseCode=关联卡密内容
deviceId=绑定设备内部数字主键
appId=应用包名 / 应用标识
versionCode=正整数版本码
versionName=展示版本名称
downloadUrl=版本文件下载地址
changelog=更新说明
forceUpdate=强制更新标记；客户端当前策略还可能要求版本码完全一致
title=公告标题或自定义头衔；参照各接口限制
reason=头衔申请理由
mode=预设模式
rules=解密后的替换规则文本
emoticons=解密后的颜文字文本
prefixEnabled=是否开启前缀
prefixText=前缀文本
appendEnabled=是否开启后缀
appendText=后缀文本
emoticonEnabled=是否开启颜文字
valid=验证是否成功
accepted=设备是否已获得授权
changed=修改是否成功
bypass=卡密验证关闭时为true，此时卡密会话token为空
serverTime=服务器本地ISO日期时间
donateUrl=赞助页面URL
afdianUserId=爱发电用户ID
afdianToken=爱发电服务密钥，仅写入，读取不返回；空白保持旧值
licenseValidationEnabled=是否启用卡密验证
accessibilityOpen=是否开放无障碍模式
floatingWindowOpen=是否开放悬浮窗模式
rootOpen=是否开放Root模式
shizukuOpen=是否开放Shizuku模式
keyboardOpen=是否开放键盘模式
presetUploadOpen=是否开放预设上传
presetDownloadOpen=是否开放预设下载
uploadOpen=预设上传是否开放
downloadOpen=预设下载是否开放
items=结果列表
amount=赞助金额字符串
plan=赞助计划名称
""".strip().splitlines())
for name, schema in schemas.items():
 for key, prop in schema.get('properties',{}).items():
  if '$ref' not in prop: prop.setdefault('description',labels.get(key,key+'（以接口说明为准）'))
  if key.endswith('At') or key.endswith('Until') or key in ['licenseExpiresAt','serverTime']:
   if prop.get('type')=='string': prop.update(nullable=True,example='2026-09-05T12:00:00')
  if key in ['accountId','uploaderId']:prop.update(type='string',example='123456789');prop.pop('format',None)
  if key in ['password','token','afdianToken']:prop['example']='<'+key.upper()+'>'
schemas['LicenseKey']['properties']['status']['enum']=['UNUSED','ACTIVE','DISABLED','EXPIRED']
for name in ['TitleItem','MyTitle']:schemas[name]['properties']['status']['enum']=['PENDING','APPROVED','REJECTED']
# Defaults for primitive booleans reflect Jackson, not a mandatory true value.
for name in ['AnnouncementSave','VersionSave']:
 for prop in schemas[name]['properties'].values():
  if prop.get('type')=='boolean':prop['default']=False
for path,methods in paths.items():
 for method,op in methods.items():
  op['description']+=' 用户会话无效可能返回HTTP 400（业务异常），不应只处理401。' if op['security']==[{'user':[]}] else ''
  if path==C+'/presets' and method=='post':op['description']+=' 注意Spring当前未显式配置multipart限制，默认单文件1MB可能先于业务2MB上限生效；建议控制在1MB内。'
  if path==A+'/titles/users/{id}':op['description']+=' 返回脱敏角色字段，不含密码哈希或头像二进制。'
  if path.endswith('/me/password'):op['description']+=' 修改密码后撤销全部既有用户会话，必须重新登录。'
  if path==C+'/login':op['description']+=' 正常登录不返回bypass字段。该接口不替代validate-card的使用次数/云端设备授权校验。'
  if path in [C+'/version',C+'/login',C+'/heartbeat']:op['responses']['200']['content']['application/json']['schema']['properties']['data']['nullable']=True
spec['info']['description']+=' 单文件上传限制2MB，总请求3MB。匿名WebSocket已关闭，请使用鉴权REST轮询。'

# Operation-specific query enums and map-body validation documented from services.
for path,methods in paths.items():
 for method,op in methods.items():
  for p in op['parameters']:
   if p['in']!='query':continue
   k=p['name']; p['description']=labels.get(k,{'after':'增量消息ID游标，<=0返回最近100条','hours':'历史小时数，默认24','keyword':'用户名或账号ID子串；空字符串返回全部','minutes':'禁言分钟数，<=0解除，最多43200','role':'社区角色，不是管理员后台权限','banned':'是否封禁公共聊天','publicVisible':'是否允许公开'}.get(k,'见接口说明'))
   if k=='status':p['schema']['enum']=['APPROVED','REJECTED'] if '/titles/' in path else ['UNUSED','ACTIVE','DISABLED','EXPIRED']
   if k=='role':p['schema']['enum']=['USER','AUTHOR','SYSTEM_ADMIN']
   if k=='appId':p['schema']['example']='com.example.u7e5f3218e9'
   if k=='count':p['schema'].update(minimum=1,maximum=100)
  for mime,item in op.get('requestBody',{}).get('content',{}).items():
   bs=item['schema']
   if '$ref' in bs:continue
   for k,v in bs.get('properties',{}).items():
    v.setdefault('description',labels.get(k,{'approved':'是否审核通过；必须显式传入，否则默认false'}.get(k,k)))
   required=[]
   if path.endswith('/users/login'):required=['login','password']
   elif path==R+'/login':required=['username','password']
   elif path.endswith('/me/name'):required=['username']
   elif path.endswith('/me/password'):required=['oldPassword','newPassword','confirmPassword']
   elif path==C+'/chat/messages':required=['content']
   elif path==C+'/users/titles':required=['title']
   if required:bs['required']=required
schemas['Empty']['example']=None
schemas['Credentials']['properties']['password']['description']='只在生成或重置操作返回的明文密码；关闭结果前安全保存，勿记录日志'

# Practical examples, including valid-length placeholder passwords for multipart.
for path,methods in paths.items():
 for op in methods.values():
  for mime,item in op.get('requestBody',{}).get('content',{}).items():
   bs=item['schema'];bs=schemas[bs['$ref'].split('/')[-1]] if '$ref' in bs else bs
   for k,v in bs.get('properties',{}).items():
    if k in ['password','confirmPassword']:v.update(example='<PASSWORD>',minLength=6,maxLength=64) if path==C+'/users/register' else v.setdefault('example','<PASSWORD>')
    if k=='avatar':v['description']='可选头像文件，最大200KB'
    if k=='file':v['description']='上传文件；头像最大200KB，预设业务2MB且注意Spring默认1MB限制'
    if k=='downloadUrl':v['example']='https://example.com/app.apk'
    if k=='donateUrl':v['example']='https://example.com/donate'
    if k=='appId':v['example']='com.example.u7e5f3218e9'
  for response in op['responses'].values():
   wrapper=response.get('content',{}).get('application/json',{}).get('schema',{})
   if 'properties' not in wrapper:continue
   for k,d in {'code':'业务状态码，0为成功','message':'结果或错误说明','timestamp':'Unix毫秒时间戳','data':'业务结果；可能为null，结构见下方'}.items():wrapper['properties'][k]['description']=d
   wrapper['properties']['timestamp']['example']=1788609600000

# All schema references must resolve.
def check(x):
 if isinstance(x,dict):
  if '$ref' in x:assert x['$ref'].split('/')[-1] in schemas,x
  for v in x.values():check(v)
 elif isinstance(x,list):
  for v in x:check(v)
check(spec)
OUT.joinpath('openapi.json').write_text(json.dumps(spec,ensure_ascii=False,indent=2))
OUT.joinpath('api-catalog.json').write_text(json.dumps(entries,ensure_ascii=False,indent=2))
print('Documented HTTP operations:',len(entries),'paths:',len(paths),'schemas:',len(schemas))
