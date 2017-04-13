package com.catfish.valida;

import com.baidu.unbiz.fluentvalidator.Validator;
import com.baidu.unbiz.fluentvalidator.ValidatorContext;
import com.baidu.unbiz.fluentvalidator.ValidatorHandler;

/**
 * Created by A on 2017/4/13.
 */
public class CarSeatCountValidator extends ValidatorHandler<Object> implements Validator<Object> {

    @Override
    public boolean validate(ValidatorContext context, Object t) {
        if (t ==null) {
            context.addErrorMsg(String.format("Seat count is not valid, invalid value=%s", t));
            return false;
        }
        return true;
    }
}