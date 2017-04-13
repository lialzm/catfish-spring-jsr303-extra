package com.catfish;

import org.springframework.validation.BindingResult;

import javax.validation.Validator;

/**
 * Created by A on 2017/4/12.
 */
public class DefaultValidateCallback implements ValidateCallback {
    @Override
    public void onSuccess(ValidatorElementList validatorElementList) {

    }

    @Override
    public void onFail(ValidatorElementList validatorElementList, BindingResult errors) {

    }

    @Override
    public void onUncaughtException(Validator validator, Exception e, Object target) throws Exception {

    }
}
