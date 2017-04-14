package com.catfish.controller;

import com.baidu.unbiz.fluentvalidator.FluentValidator;
import com.baidu.unbiz.fluentvalidator.Result;
import com.catfish.service.UserService;
import com.catfish.valida.CarSeatCountValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Past;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @InitBinder
    public void initBinder(WebDataBinder binder) throws Exception {
        //注册自定义的属性编辑器
        //1、日期
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        CustomDateEditor dateEditor = new CustomDateEditor(df, true);
        //表示如果命令对象有Date类型的属性，将使用该属性编辑器进行类型转换
        binder.registerCustomEditor(Date.class, dateEditor);
    }

    @RequestMapping("/testValidaGeneric")
    @ResponseBody
    public String testValidaGeneric(@Past Date test) {
        return "";
    }



}
