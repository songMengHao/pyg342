package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

/**
 * 进入到这里的请求都是经过单点登录认证的,在这里获取这个用户具有哪些权限,封装成集合返还给spring-security
 */
public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ArrayList<GrantedAuthority> list = new ArrayList<>();
        //向权限集合中加入权限对象
        list.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username,"",list);
    }
}
