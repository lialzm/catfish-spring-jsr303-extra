package com.catfish.controller;

import com.catfish.annotation.FlowValid;
import com.catfish.validator.FlowValidator;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by apple on 17/4/6.
 */
@Controller
@Validated
public class TestController {


    @FlowValid({NotEmpty.class})
    public void abc(){

    }

    @RequestMapping("/test")
    public String test(String name) {
        System.out.println("1111");
        new FlowValidator().valid();
        return "";
    }

    @RequestMapping("/test2")
    public String test2(String name) {
        FlowValidator.doValid(name);
        return "";
    }


}
