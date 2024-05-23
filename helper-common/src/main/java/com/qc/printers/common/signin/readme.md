# 规范定义

！！注意使用同一个`consul` `192.168.12.12这个[通过10.15.247.254网关来互通]`

## 安全定义

每个设备在注册到consul的时候必须

- service_name = `signin`
- service_address = `打卡设备的运行ip【必须为10段】`
- service_port = `打卡设备的运行端口号【必须同网段可以访问】`
- service_tags = `["signin"]`
- service_meta = `{"zc": "face,"}` 这个必须为zc，值为支持的方式逗号分割
- service_id = `每个设备唯一的设备id，字符串`
- secert = `不注册，密钥，定义在设备的config里，除了health接口都需要校验`

## 接口定义

### 人脸数据列表接口[必须]

<table>
<capital>注意请求的请求头都得有secert_h值为设备密钥</capital>
<tr>
<th> Support字段[基本一个设备也就支持一种，不管几种，都单独实现就完了] </th>
<th>功能</th>
<th >生物数据List接口</th>
<th >接口请求方式</th>
<th >请求值</th>
<td >返回值[JSON]  </td>
</tr>
<tr>
<th rowspan="2">face</th>
<td>拉取设备人脸列表</td>
<td>list_all_face</td>
<td>GET</td>
<td>无</td>
<td>[{"student_id":"","username":"","face_data":""}...]</td>
</tr>
<tr>
<td>向设备上传人脸数据</td>
<td>sync_upload</td>
<td>POST</td>
<td>{"model":"ADD或者NEW","data":[{"username":"","student_id":"","is_none":"","face_data":""}...]}</td>
<td>[{student_id:"",username:"",face_data:""}...]</td>
</tr>


</table>

### consul心跳接口[必须]

上面注册的address+port+/health能访问即可[GET]返回值任意,状态码200

