package com.bizfns.services.Utility;

import com.bizfns.services.GlobalDto.GlobalResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class AccessTokenValidation {

    public boolean checkUserMatch(String tenantId , String username){

        String[] part = username.split(",");

        //String userPhone = profileRepository.checkAccessToken(userId, tenantId);

        String tokenUserId = part[0];
        String tokenTenantId = part[1];
        if (!tenantId.equals(tokenTenantId)){
            return true;
        }

        return false;
    }
   /*
    if(checkUserMatch(userId,tenantId, principal.getName())){
        return ResponseEntity.badRequest().body(new GlobalResponseDTO(false, "Unauthorised user, we could not access the APIs from others token "));
    }


    */
}
