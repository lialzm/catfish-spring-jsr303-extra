package com.catfish.controller;

import com.catfish.DefaultValidateCallback;
import com.catfish.ValidateCallback;
import com.catfish.ValidatorElementList;
import com.catfish.annotation.FlowValid;
import com.catfish.annotation.Price;
import com.catfish.constraint.CrossParameter2;
import com.catfish.constraint.Group1;
import com.catfish.element.User;
import com.catfish.valida.Valida1;
import com.catfish.validator.FlowValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by A on 2017/3/22.
 */

@Controller
@FlowValid(value = Valida1.class)
@RequestMapping("/flow")
public class FlowController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ValidatorFactory validatorFactory;

    /**
     * 校验bean
     *
     * @param user
     * @return
     */
    @RequestMapping("/testBean")
    @ResponseBody
    public String testBean(@ModelAttribute User user) {
        BindingResult bindingResult = FlowValidator.check(validatorFactory)
                .on(Valid.class, user).valida().end();
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().get(0);
            return objectError.getObjectName() + objectError.getDefaultMessage();
        }
        logger.info(user.toString());
        return "";
    }


    /**
     * 校验基本注解
     *
     * @return
     */
    @RequestMapping("/testValidaGeneric")
    @ResponseBody
    public String testValidaGeneric() {
        String test = null;
        BindingResult bindingResult = FlowValidator.check(validatorFactory).on(NotNull.class, "test", test).valida().end();
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().get(0);
            return objectError.getObjectName() + objectError.getDefaultMessage();
        }
        return "";
    }


    @RequestMapping("/testValidaGeneric2")
    @ResponseBody
    public String testValidaGeneric2() {
        BindingResult bindingResult = FlowValidator.check(validatorFactory)
                .on(NotNull.class, new Class[]{Group1.class}, "test", null)
                .valida()
                .end();
        String test = null;
        long start = System.currentTimeMillis();
        if (test==null){
            int timeElapsed = (int) (System.currentTimeMillis() - start);
            logger.debug("耗时=====:" + timeElapsed);
        }
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().get(0);
            return objectError.getDefaultMessage();
        }
        return "";
    }

    /**
     * 校验注解组合
     *
     * @param price
     * @return
     */
    @RequestMapping("/testValidaCompose")
    @ResponseBody
    public String testValidaCompose(String price) {
        BindingResult bindingResult = FlowValidator.check(validatorFactory)
                .on(Price.class, new Class[]{Group1.class}, "价格", price).valida().end();
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().get(0);
            return objectError.getObjectName() + objectError.getDefaultMessage();
        }
        return "";
    }

    @RequestMapping("/testValidaCompose2")
    @ResponseBody
    public String testValidaCompose2(String time) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d H:m:s");
        // 指定一个日期
        Date date = null;
        try {
            date = dateFormat.parse("2018-6-1 13:24:16");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 对 calendar 设置为 date 所定的日期
        calendar.setTime(date);
        BindingResult bindingResult = FlowValidator.check(validatorFactory).on(Past.class, "时间", calendar).<Past,Date>valida(new DefaultValidateCallback()).end();
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().get(0);
            return objectError.getObjectName() + objectError.getDefaultMessage();
        }
        return "";
    }

    /**
     * 校验多参数注解
     *
     * @return
     */
    @RequestMapping("/changePassword2")
    @ResponseBody
    public String changePassword2() {
        Object[] objects = new Object[]{"", "1"};
        BindingResult bindingResult = FlowValidator.check(validatorFactory).on(CrossParameter2.class, objects).valida().end();
        if (bindingResult.hasErrors()) {
            ObjectError objectError = bindingResult.getAllErrors().get(0);
            return objectError.getObjectName() + objectError.getDefaultMessage();
        }
        return "";
    }

    @RequestMapping("/callback")
    @ResponseBody
    public String callback() {
        FlowValidator.check(validatorFactory)
                .on(NotNull.class, new Class[]{Group1.class}, null
        ).valida(new ValidateCallback() {
            @Override
            public void onSuccess(ValidatorElementList validatorElementList) {

            }

            @Override
            public void onFail(ValidatorElementList validatorElementList, BindingResult errors) {
                logger.info("errors111==" + errors.getAllErrors().get(0).getDefaultMessage());
            }

            @Override
            public void onUncaughtException(Validator validator, Exception e, Object target) throws Exception {

            }
        }).on(NotNull.class, null).valida(new ValidateCallback() {
            @Override
            public void onSuccess(ValidatorElementList validatorElementList) {

            }

            @Override
            public void onFail(ValidatorElementList validatorElementList, BindingResult errors) {
                logger.info("errors222==" + errors.getAllErrors().get(0).getDefaultMessage());
//                logger.info("size==" + validatorElementList.size());

            }

            @Override
            public void onUncaughtException(Validator validator, Exception e, Object target) throws Exception {

            }
        }).end();
        return "";
    }

    @RequestMapping("/when")
    @ResponseBody
    public String when() {
        BindingResult result = FlowValidator.check(validatorFactory).when(false)
                .on(NotNull.class,new Class[]{Group1.class},null)
                .when(true)
                .on(NotNull.class,null)
                .valida().end();
        System.out.println(result);
        return result.getAllErrors().get(0).getDefaultMessage();
    }

}
