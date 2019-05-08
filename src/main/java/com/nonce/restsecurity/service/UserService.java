package com.nonce.restsecurity.service;

import com.nonce.restsecurity.config.UrlResponse;
import com.nonce.restsecurity.dao.AuthorityUserRepository;
import com.nonce.restsecurity.util.SecurityResponse;
import com.nonce.restsecurity.util.TimeUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andon
 * @date 2019/3/20
 */
@SuppressWarnings("Duplicates")
@Service
@Transactional
public class UserService {

    @Resource
    private AuthorityUserRepository authorityUserRepository;

    /**
     * 根据用户名查询密码
     */
    public String findPasswordByUsernameAfterValidTime(String username) {
        String nowTime = TimeUtil.FORMAT.get().format(System.currentTimeMillis());
        return authorityUserRepository.findPasswordByUsernameAfterValidTime(username, nowTime);
    }

    /**
     * 根据用户名获得角色名称
     */
    public List<String> findRoleNameByUsername(String username) {
        return authorityUserRepository.findRoleNameByUsername(username);
    }

    /**
     * 根据用户名获得菜单信息
     */
    public Map<String, Object> findMenuInfoByUsername(String username, UrlResponse response) {
        Map<String, Object> userIdAndNickNameByUsername = authorityUserRepository.findUserIdAndNickNameAndRemarkByUsername(username);
        int userId = (int) userIdAndNickNameByUsername.get("id");
        String nickname = (String) userIdAndNickNameByUsername.get("nickname");
        String remark = (String) userIdAndNickNameByUsername.get("remark");
        Map<String, Object> userInfo = new HashMap<>();
        List<Map<String, Object>> menuInfoList = new ArrayList<>();
        // 判断是否最高权限
        List<String> rootUrlByUsername = authorityUserRepository.findUrlsByUsername(username);
        boolean isHighestAuthority = rootUrlByUsername.contains("/**");
        if (isHighestAuthority) {
            List<Map<String, Object>> rootMenuInfoList = authorityUserRepository.findRootMenuInfo();
            rootMenuInfoList.forEach(rootMenuInfo -> {
                int id = (int) rootMenuInfo.get("id");
                List<Map<String, Object>> children = authorityUserRepository.findMenuInfoByParentId(id);
                Map<String, Object> map = new HashMap<>();
                map.put("id", id);
                map.put("url", rootMenuInfo.get("url"));
                map.put("menuName", rootMenuInfo.get("menuName"));
                map.put("parentId", rootMenuInfo.get("parentId"));
                map.put("remark", rootMenuInfo.get("remark"));
                map.put("urlPre", rootMenuInfo.get("urlPre"));
                map.put("children", children);
                menuInfoList.add(map);
            });
        } else {
            List<Map<String, Object>> rootMenuInfoByUsername = authorityUserRepository.findRootMenuInfoByUsername(username);
            if (!ObjectUtils.isEmpty(rootMenuInfoByUsername)) {
                rootMenuInfoByUsername.forEach(rootMenuInfo -> {
                    int id = (int) rootMenuInfo.get("id");
                    List<Map<String, Object>> children = authorityUserRepository.findMenuInfoByParentId(id);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", id);
                    map.put("url", rootMenuInfo.get("url"));
                    map.put("menuName", rootMenuInfo.get("menuName"));
                    map.put("parentId", rootMenuInfo.get("parentId"));
                    map.put("remark", rootMenuInfo.get("remark"));
                    map.put("urlPre", rootMenuInfo.get("urlPre"));
                    map.put("children", children);
                    menuInfoList.add(map);
                });
            }
            List<Integer> rootMenuIdOfPartialPermission = authorityUserRepository.findRootMenuIdOfPartialPermission(username);
            if (!ObjectUtils.isEmpty(rootMenuIdOfPartialPermission)) {
                rootMenuIdOfPartialPermission.forEach(menuId -> {
                    Map<String, Object> rootMenuInfo = authorityUserRepository.findMenuInfoByMenuId(menuId);
                    List<Map<String, Object>> children = authorityUserRepository.findChildrenMenuInfoByUsernameAndParentId(username, menuId);
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", menuId);
                    map.put("url", rootMenuInfo.get("url"));
                    map.put("menuName", rootMenuInfo.get("menuName"));
                    map.put("parentId", rootMenuInfo.get("parentId"));
                    map.put("remark", rootMenuInfo.get("remark"));
                    map.put("urlPre", rootMenuInfo.get("urlPre"));
                    map.put("children", children);
                    menuInfoList.add(map);
                });
            }
        }
        userInfo.put("nickname", nickname);
        userInfo.put("userId", userId);
        userInfo.put("username", username);
        userInfo.put("remark", remark);
        userInfo.put("menuList", menuInfoList);
        if (!ObjectUtils.isEmpty(menuInfoList)) {
            response.setTotal(menuInfoList.size());
        }
        return userInfo;
    }

    /**
     * 获得所有的菜单url
     */
    public List<String> findAllMenuUrl() {
        return authorityUserRepository.findAllMenuUrl();
    }

    /**
     * 根据菜单url获得需要拥有的角色
     */
    public List<String> findRoleNameByMenuUrl(String url) {
        return authorityUserRepository.findRoleNameByMenuUrl(url);
    }

    /**
     * 查询用户名是否已存在 存在:返回true
     */
    public boolean usernameIsExistence(String username) {
        int count = authorityUserRepository.findCountByUsername(username);
        return count == 1;
    }

    /**
     * 新增用户信息
     */
    public void addUserInfo(String nickname, String username, String password, String email, String phone, String validTime, String remark) {
        String encode = new BCryptPasswordEncoder().encode(password);
        long timeId = System.currentTimeMillis();
        String nowTime = TimeUtil.FORMAT.get().format(timeId);
        if (ObjectUtils.isEmpty(validTime)) {
            String validTimeDefault = TimeUtil.FORMAT.get().format(timeId + 7 * 24 * 60 * 60 * 1000);
            authorityUserRepository.addUserInfo(nickname, username, encode, email, phone, validTimeDefault, nowTime, remark);
        } else {
            authorityUserRepository.addUserInfo(nickname, username, encode, email, phone, validTime, nowTime, remark);
        }
    }

    /**
     * 为用户分配角色(批量角色id:逗号分隔)
     */
    public void addRolesForUser(String userId, String roleIds) {
        int uId = Integer.parseInt(userId);
        authorityUserRepository.deleteRolesByUserId(uId);
        String nowTime = TimeUtil.FORMAT.get().format(System.currentTimeMillis());
        String[] roleIdsArray = roleIds.split(",");
        for (String roleId : roleIdsArray) {
            int rId = Integer.parseInt(roleId);
            authorityUserRepository.addRoleForUser(uId, rId, nowTime);
        }
    }

    /**
     * 删除用户信息 (批量删除id 逗号分隔)
     */
    public void deleteUserInfo(String id) {
        String[] ids = id.split(",");
        for (String userId : ids) {
            int uId = Integer.parseInt(userId);
            authorityUserRepository.deleteRolesByUserId(uId);
            authorityUserRepository.deleteUserInfoByUserId(uId);
            authorityUserRepository.deleteUserCollectionByUserId(uId);
        }
    }

    /**
     * 修改用户信息
     */
    public void updateUserInfo(String id, String nickname, String username, String password, String email, String phone, String validTime, String remark) {
        int userId = Integer.parseInt(id);
        String nowTime = TimeUtil.FORMAT.get().format(System.currentTimeMillis());
        if (ObjectUtils.isEmpty(validTime)) {
            if (ObjectUtils.isEmpty(password)) {
                authorityUserRepository.updateUserInfoByUserIdExcludeValidTimeAndPassword(userId, nickname, username, email, phone, nowTime, remark);
            } else {
                String encode = new BCryptPasswordEncoder().encode(password);
                authorityUserRepository.updateUserInfoByUserIdExcludeValidTime(userId, nickname, username, encode, email, phone, nowTime, remark);
            }
        } else {
            authorityUserRepository.updateUserInfoByUserIdExcludePassword(userId, nickname, username, email, phone, validTime, nowTime, remark);
        }

        if (ObjectUtils.isEmpty(validTime) && !ObjectUtils.isEmpty(password)) {
            String encode = new BCryptPasswordEncoder().encode(password);
            authorityUserRepository.updateUserInfoByUserIdExcludeValidTime(userId, nickname, username, encode, email, phone, nowTime, remark);
        } else if (!ObjectUtils.isEmpty(validTime) && ObjectUtils.isEmpty(password)) {
            authorityUserRepository.updateUserInfoByUserIdExcludePassword(userId, nickname, username, email, phone, validTime, nowTime, remark);
        }
    }
    
    /**
     * 获取所有的用户信息
     */
    public List<Map<String, Object>> findAllUserInfo(String pageNum, String pageSize, String username, String nickname, SecurityResponse securityResponse) {
        int rowNum = Integer.parseInt(pageNum);
        int size = Integer.parseInt(pageSize);
        int row = (rowNum - 1) * size;
        String uName = "%" + username + "%";
        String nName = "%" + nickname + "%";
        System.out.println("username >> " + username);
        List<Map<String, Object>> list = new ArrayList<>();
        List<Map<String, Object>> allUserInfo = authorityUserRepository.findAllUserInfo(row, size, uName, nName);
        for (Map<String, Object> userInfo : allUserInfo) {
            int userId = (int) userInfo.get("id");
            List<Map<String, Object>> roleInfo = authorityUserRepository.findRoleInfoByUserId(userId);
            Map<String, Object> map = new HashMap<>();
            map.put("id", userId);
            map.put("nickname", userInfo.get("nickname"));
            map.put("username", userInfo.get("username"));
            map.put("password", userInfo.get("password"));
            map.put("email", userInfo.get("email"));
            map.put("phone", userInfo.get("phone"));
            map.put("validTime", userInfo.get("validTime"));
            map.put("remark", userInfo.get("remark"));
            map.put("roleList", roleInfo);
            list.add(map);
        }
        int userInfoSize = authorityUserRepository.findAllUserInfoSize(uName, nName);
        securityResponse.setSuccess(true);
        securityResponse.setCode("1");
        securityResponse.setMessage("Find all user success!!");
        securityResponse.setData(list);
        securityResponse.setTotal(userInfoSize);
        return list;
    }

    /**
     * 修改的用户名是否存在 不存在 返回true
     */
    public boolean isNotExistenceOfUpdateUsername(String id, String username) {
        int userId = Integer.parseInt(id);
        int count = authorityUserRepository.isNotExistenceOfUpdateUsername(userId, username);
        return count == 0;
    }
}
