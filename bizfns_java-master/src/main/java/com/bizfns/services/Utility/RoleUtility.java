package com.bizfns.services.Utility;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class RoleUtility {

    public String getAuthority(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        ArrayList role = new ArrayList();
        for (GrantedAuthority authority : authorities){
            String authority1 = authority.getAuthority();
            role.add(authority1);
        }
        return (String) role.get(0);
    }

}
