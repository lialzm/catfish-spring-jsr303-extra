package com.catfish;

import org.springframework.validation.BindingResult;

import javax.validation.Validator;

/**
 * 校验回调函数
 * Created by A on 2017/4/12.
 */
public interface ValidateCallback {

    /**
     * 所有验证完成并且成功后
     *
     * @param validatorElementList 验证器list
     */
    void onSuccess(ValidatorElementList validatorElementList);

    /**
     * 所有验证步骤结束，发现验证存在失败后
     *
     * @param validatorElementList 验证器list
     * @param errors               验证过程中发生的错误
     */
    void onFail(ValidatorElementList validatorElementList, BindingResult errors);

    /**
     * 执行验证过程中发生了异常后
     *
     * @param validator 验证器
     * @param e         异常
     * @param target    正在验证的对象
     * @throws Exception
     */
    void onUncaughtException(Validator validator, Exception e, Object target) throws Exception;

}
