package com.catfish.element;

import com.catfish.ValidatorElementList;

import java.io.Serializable;

/**
 * Created by A on 2017/4/13.
 */
public class WhenElement implements Serializable {

    private ValidatorElementList<ValidaElement> validatorElements=new ValidatorElementList<>();
    private Boolean expression;

    public ValidatorElementList<ValidaElement> getValidatorElements() {
        return validatorElements;
    }

    public void setValidatorElements(ValidatorElementList<ValidaElement> validatorElements) {
        this.validatorElements = validatorElements;
    }



    public Boolean getExpression() {
        return expression;
    }

    public void setExpression(Boolean expression) {
        this.expression = expression;
    }
}
