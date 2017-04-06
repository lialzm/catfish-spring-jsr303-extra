package com.catfish.controller;

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

    @RequestMapping("/test")
    public String test(@NotEmpty String name) {
        System.out.println("1111");
        return "";
    }

    @RequestMapping("/test2")
    public String test2(String name) {
        FlowValidator.doValid(name);
        return "";
    }


}
