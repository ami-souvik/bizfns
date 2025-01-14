package com.bizfns.services.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Userinfo {

    private String password;
    private String username;
    private String roles;

}
