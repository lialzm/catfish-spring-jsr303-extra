package com.catfish.element;

import com.catfish.ValidatorElementList;

/**
 * Created by A on 2017/4/12.
 */
public class FlowMetaData {

    private ValidatorElementList<WhenElement>  whenElementList=new ValidatorElementList<>();

    public ValidatorElementList<WhenElement> getWhenElementList() {
        return whenElementList;
    }

    public void setWhenElementList(ValidatorElementList<WhenElement> whenElementList) {
        this.whenElementList = whenElementList;
    }
}
