package com.bizfns.services.Config;


import com.bizfns.services.Entity.Userinfo;
import com.bizfns.services.Repository.UserInfoRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


public class UserDetailsConfig implements UserDetails {


    private String username;
    private String password;
    private List<GrantedAuthority> authorities;

    public UserDetailsConfig(Userinfo userInfo) {

        username=userInfo.getUsername();
        password = userInfo.getPassword();
        authorities= Arrays.stream(userInfo.getRoles().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

    }

//    public static UserDetailsConfig loadUserByUsername(String username, UserInfoRepository userInfoRepository) {
//        Userinfo userInfo = userInfoRepository.findByUsername(username);
//        return new UserDetailsConfig( userInfo);
//    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}


