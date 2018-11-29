# Spring Security

> 	Spring Security是一个功能强大且可高度自定义的身份验证和访问控制框架。它是保护基于Spring的应用程序的事实上的标准。

> 	Spring Security是一个专注于为Java应用程序提供身份验证和授权的框架。与所有Spring项目一样，Spring Security的真正强大之处在于它可以轻松扩展以满足自定义要求。

**项目完整功能：**
 1. 登录（根据所登录用户所具有的权限返回给前端JSON数据，动态的展示功能菜单）。
 2. 注销。
 3. 动态拦截url请求，根据当前登录用户所具有的权限，进行权限认证（防止不具有权限的用户直接通过url请求功能）。    
5. 用户管理模块（查询所有用户、新增用户、修改用户、删除用户、为用户添加角色）。
6. 角色管理模块（查询所有角色、新增角色、修改角色、删除角色、为角色添加可访问的资源菜单）。
7. 菜单管理模块（查询所有菜单、新增菜单、修改菜单、删除菜单）。




跳转: [**WebSecurityConfigure.java**](https://github.com/TianShengBingFeiNiuRen/SpringBoot_SpringSecurity/blob/master/src/main/java/com/andon/securitydemo/config/WebSecurityConfigure.java)

**CSDN**: [**link**](https://blog.csdn.net/weixin_39792935/article/details/84541194).
