package com.catfish.service;

import com.catfish.constraint.CrossParameter;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Created by A on 2017/4/11.
 */
@Service
@Validated
public class UserService {

    @CrossParameter
    public void changePassword(String password,String confirmation){

    }

}
