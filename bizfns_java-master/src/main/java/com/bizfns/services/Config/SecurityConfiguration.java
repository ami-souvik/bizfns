package com.bizfns.services.Config;


//import com.dailycodebuffer.jwt.filter.JwtFilter;
//import com.dailycodebuffer.jwt.service.UserService;
import com.bizfns.services.Filter.JwtFilter;
import com.bizfns.services.Serviceimpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter{

    @Autowired
    private UserService userService;

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(userService);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedOrigin("*"); // Allow requests from any origin
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/api/users/userlogin","/api/users/clientList/**")
                .permitAll()
                .antMatchers("/api/users/testapi","/api/users/addTaxTable")
                .permitAll()
                .antMatchers("/api/users/companyRegistration","api/profile/**/")
                .permitAll()
                .antMatchers("/api/users/testMail","/api/users/staffUserLogin","/api/users/save_user_priviledges")
                .permitAll()
                .antMatchers("/api/users/otpVerification")
                .permitAll()
                .antMatchers("/api/users/fetchPreRegistration","/api/docs/**")
                .permitAll()
                .antMatchers("/api/users/forgotPassword","/api/users/invoices/**")
                .permitAll()
                .antMatchers("/api/users/validateForgotPasswordOtp")
                .permitAll()
                .antMatchers("/api/users/resetPassword")
                .permitAll()
                .antMatchers("/api/users/testSchemat")
                .permitAll()
                .antMatchers("/api/users/appLink")
                .permitAll()
                .antMatchers("/api/users/preregistrationSendOtp")
                .permitAll()
                .antMatchers("/api/users/preregistrationOtpVerification")
                .permitAll()
                .antMatchers("/api/users/testFor")
                .permitAll()
                .antMatchers("/api/users/getActiveStatusForStaff")
                .permitAll()
                .antMatchers("/api/users/forgotBusinessId")
                .permitAll()
                .antMatchers("/api/users/phoneNoRegCheck")
                .permitAll()
                .antMatchers("/api/users/downloadMediafile/{imageName}")
                .permitAll()
                .antMatchers("/api/users/downloadInvoiceFile/{invoice}")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    }


}

