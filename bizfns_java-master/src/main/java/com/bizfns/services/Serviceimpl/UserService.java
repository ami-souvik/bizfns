package com.bizfns.services.Serviceimpl;

import com.bizfns.services.Config.UserDetailsConfig;
import com.bizfns.services.Entity.Userinfo;
import com.bizfns.services.Query.StaffAuthQuery;
import com.bizfns.services.Repository.CompanyUserRepository;
import com.bizfns.services.Repository.UserInfoRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private CompanyUserRepository companyUserRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private StaffAuthQuery staffAuthQuery;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {

        //code for role we can write for that we have to call the query method



       // userInfoRepository.findByUsername(userName);

        AES obj = new AES();
        String decryptedDbPassword = null;


        String dataForPasswordValidationFromCompanyuse="";


        String[] parts = userName.split(",");

        String extractUserName = parts[0];
        String extractTenantId="";

        if(parts.length>1) {
            extractTenantId = parts[1];
        }else{
            extractTenantId = parts[0];
        }

        String dataForPasswordValidationFromCompanyMaster1 = companyUserRepository.dataForPasswordValidationFromCompanyMaster(extractUserName,extractTenantId);

        decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyMaster1);


        //for role by Huzefa
        String role = userInfoRepository.findByUsername(extractUserName,extractTenantId);

        Userinfo userInfo = new Userinfo();
        userInfo.setUsername(userName);
        userInfo.setPassword(decryptedDbPassword);
        userInfo.setRoles(role);
        //userInfo.setRoles(role);
        //System.err.println("user details :::" +userInfo.getUsername());




//        if(parts.length>1) {
//            extractTenantId = parts[1];
//        }else{
//            extractTenantId = parts[0];
//        }


        String lastTwoTLetters = null;
        String tenantFirstEightTLetters = null;

        if (extractTenantId != null && extractTenantId.length() >= 8) {
            lastTwoTLetters = extractTenantId.substring(extractTenantId.length() - 2);
            tenantFirstEightTLetters = extractTenantId.substring(0, 8);

        }


        if(lastTwoTLetters.equalsIgnoreCase("st")){
               String staffPassword = staffAuthQuery.staffDbPassword(extractUserName,tenantFirstEightTLetters);
            decryptedDbPassword = obj.decrypt(staffPassword);

            //huzefa
            return new User(userName, decryptedDbPassword, AuthorityUtils.createAuthorityList(role));

            //return new User(userName, decryptedDbPassword, new ArrayList<>());
        } else {
            String dataForPasswordValidationFromCompanyMaster = companyUserRepository.dataForPasswordValidationFromCompanyMaster(extractUserName, extractTenantId);
            if(dataForPasswordValidationFromCompanyMaster==null){
                String dataForPasswordValidationFromCompanyuser = staffAuthQuery.staffDbPasswordForStaff(extractUserName,tenantFirstEightTLetters);
                decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyuser);
                return new User(userName, decryptedDbPassword, AuthorityUtils.createAuthorityList(role));
            }
            decryptedDbPassword = obj.decrypt(dataForPasswordValidationFromCompanyMaster);
            return new User(userName, decryptedDbPassword,AuthorityUtils.createAuthorityList(role));
        }
    }
    public static boolean isEmail(String userId) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";

        return userId.matches(emailPattern);
    }

    public static boolean isPhoneNumber(String userId) {
        String phonePattern = "\\d{10}";

        return userId.matches(phonePattern);
    }


}
