# 概述
关于 HappyPermissions 名字的来源，一是因为创建它的时候正好是我的生日，二是因为我希望申请权限的流程应该是轻松快乐的。
## 流程
```java
private void showContactsWithPermissionsCheck() {
    if (ContextCompat.checkSelfPermission(MainActivity.this,
            Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.READ_CONTACTS)) {
            // TODO: 弹框解释为什么需要这个权限. 【下一步】 -> 再次请求权限
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    RC_CONTACTS);
        }
    } else {
        showContacts();
    }
}
private void showContacts() {
    startActivity(ContactsActivity.getIntent(MainActivity.this));
}
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                       @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
        case RC_CONTACTS:
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.READ_CONTACTS)) {
                    // TODO: 弹框引导用户去设置页主动授予该权限. 【去设置】 -> 应用信息页
                } else {
                    // TODO: 弹框解释为什么需要这个权限. 【下一步】 -> 再次请求权限
                }
            }
            break;
        default:
            break;
    }
}
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SETTINGS) {
        // TODO: 在用户主动授予权限后重新检查权限，但不要在这里进行事务提交等生命周期敏感操作
    }
}
```
这是教科书式动态申请权限的处理逻辑，其中的 `requestCode` 用来唯一标记权限请求。为了更好地用户体验应用需要定制两个对话框，其中 “引导用户去设置页主动授予该权限” 的对话框是通用对话框，点击【去设置】只需要跳转到当前应用的应用详情页即可，跟请求的具体权限无关。而 “解释为什么需要这个权限” 的对话框与具体权限相关，点击【下一步】需要再次请求该权限（调用 `requestPermissions()` 方法）。
整个流程的代码量虽然看起来不大，但每次请求新的权限都复制粘贴这样的代码会让代码可读性变得越来越低，越来越难以维护，所以需要尽量把这部分代码从具体的业务逻辑中分离出来，尽量复用模板代码。目前有几个主流的实践思路，一个是利用工具类和运行时注解进行简单封装的 [easypermissions](https://github.com/googlesamples/easypermissions)，一个是利用编译时注解和注解处理器自动生成模板文件的 [PermissionsDispatcher](https://github.com/permissions-dispatcher/PermissionsDispatcher)，还有一个是基于 ReactiveX 将权限请求和结果作为事件进行处理的 [RxPermissions](https://github.com/tbruyelle/RxPermissions)。
## 最佳实践
- 权限请求对话框是操作系统进行管理的，应用无法也不应该干预。
- 系统对话框描述的是权限组而不是某个具体权限。
- 如果用户授予了权限组中的一个权限，那么再申请该权限组的其它权限时系统会自动授予，不需要用户再授权。但这并不意味着该权限组中的其它权限就不用申请了，因为权限处于哪个权限组将来有可能会发生变化。
- 调用 `requestPermissions()` 并不意味着系统一定会弹出权限请求对话框，也就是说不能假设调用该方法后就发生了用户交互，因为如果用户之前勾选了 “禁止后不再询问” 或者系统策略禁止应用获取权限，那么系统会直接拒绝此次权限请求，没有任何交互。
- 如果某个权限跟应用的主要功能无关，如应用中广告可能需要位置权限，用户可能很费解，此时在申请权限之前弹出对话框向用户解释为什么需要这个权限是个不错的选择。但不要在所有申请权限之前都弹出对话框解释，因为频繁地打断用户的操作或让用户进行选择容易让用户不耐烦。
- `Fragment` 中的 `onRequestPermissionsResult()` 方法只有在使用 `Fragment#requestPermissions()` 方法申请权限时才可能接收到回调，建议将权限放在所属 `Activity` 中申请和处理。

## 实践
### 目标
尽量封装但又不过渡封装，申请权限时我们需要提供的元素: 当前 `Activity`，请求码，要申请的权限列表，是否是正常工作所需权限，授权成功后要执行的回调，“解释为什么需要这个权限” 的对话框所需描述获取的回调，“引导用户去设置页主动授予该权限” 的对话框所需描述获取的回调。