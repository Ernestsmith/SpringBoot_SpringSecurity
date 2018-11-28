package com.andon.securitydemo.dao;

import com.andon.securitydemo.domain.MenuRole;
import com.andon.securitydemo.domain.MenuRolePK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRoleRepository extends JpaRepository<MenuRole, MenuRolePK> {

    Integer deleteByRoleId(Long roleId);

    Integer deleteByMenuId(Long menuId);
}
