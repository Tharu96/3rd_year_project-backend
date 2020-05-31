package lk.gedaratama.backendserver.service;

import lk.gedaratama.backendserver.config.JwtAuthenticationConfig;
import lk.gedaratama.backendserver.config.JwtGenerator;
import lk.gedaratama.backendserver.dao.UserDao;
import lk.gedaratama.backendserver.model.User;
import lk.gedaratama.backendserver.jwt.JwtUserDetailService;
import lk.gedaratama.backendserver.resource.UserResource;
import lk.gedaratama.backendserver.util.GedaratamaParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  @author Sashini Tharuka on 5/31/2020.
 */

@Service
public class UserDetailsService {
    private final UserDao userDao;
    private final PasswordEncoder bcryptEncoder;
    private final JwtAuthenticationConfig jwtAuthenticationConfig;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailService jwtUserDetailService;

    @Autowired
    public UserDetailsService(UserDao userDao, PasswordEncoder bcryptEncoder, JwtAuthenticationConfig jwtAuthenticationConfig, AuthenticationManager authenticationManager, JwtUserDetailService jwtUserDetailService) {
        this.userDao = userDao;
        this.bcryptEncoder = bcryptEncoder;
        this.jwtAuthenticationConfig = jwtAuthenticationConfig;
        this.authenticationManager = authenticationManager;
        this.jwtUserDetailService = jwtUserDetailService;
    }

    public User userRegister(UserResource user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        newUser.setEmail(user.getEmail());
        newUser.setRole(GedaratamaParam.USER_NORMAL);
        newUser.setUuid(UUID.randomUUID().toString());
        return userDao.save(newUser);
    }

    private String createAccessToken(UserResource userResource) {

        List<SimpleGrantedAuthority> grantedAuthorityList = new ArrayList<>();
        grantedAuthorityList.add(new SimpleGrantedAuthority(GedaratamaParam.ROLE_PREFIX + userResource.getRole()));

        return JwtGenerator.generateAccessJWT(userResource.getUsername(), userResource.getUuid(),
                grantedAuthorityList, jwtAuthenticationConfig.getAccessTokenExpiration(), jwtAuthenticationConfig.getSecret());
    }

    private String createRefreshToken(UserResource userResource) {
        List<SimpleGrantedAuthority> grantedAuthorityList = new ArrayList<>();
        grantedAuthorityList.add(new SimpleGrantedAuthority(GedaratamaParam.ROLE_PREFIX + userResource.getRole()));
        return JwtGenerator.generateRefreshToken(userResource.getUsername(), userResource.getUuid(),
                grantedAuthorityList, jwtAuthenticationConfig.getRefreshTokenExpiration(), jwtAuthenticationConfig.getSecret());
    }
}