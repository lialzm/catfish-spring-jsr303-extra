package com.catfish.controller;

import com.baidu.unbiz.fluentvalidator.FluentValidator;
import com.baidu.unbiz.fluentvalidator.Result;
import com.catfish.service.UserService;
import com.catfish.valida.CarSeatCountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;

import static com.baidu.unbiz.fluentvalidator.ResultCollectors.toSimple;

/**
 * Created by A on 2017/4/12.
 */
@Controller
@RequestMapping("/hibernate")
@Validated
public class HibernateController {

    @Autowired
    UserService userService;

    private Logger logger= LoggerFactory.getLogger(getClass());

    /**
     * 跨参数校验
     * @return
     */
    @RequestMapping("/changePassword")
    @ResponseBody
    public String changePassword() {
        try {
            userService.changePassword("", "1");
        }catch (ConstraintViolationException e){
           logger.info(e.getMessage());
        }
        return "";
    }

    @RequestMapping("/test")
    @ResponseBody
    public String test(){
        Result ret = FluentValidator.checkAll().on(null, new CarSeatCountValidator()).doValidate().result(toSimple());
        return "";
    }



}
