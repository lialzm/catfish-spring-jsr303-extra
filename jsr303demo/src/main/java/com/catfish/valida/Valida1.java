package com.catfish.valida;

import com.catfish.annotation.Price;
import com.catfish.constraint.CrossParameter;
import com.catfish.constraint.CrossParameter2;
import com.catfish.constraint.Group1;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;

/**
 * Created by A on 2017/4/11.
 */
public class Valida1 {

    @Valid
    @NotNull(message = "{user.id.null}")
    @Price(groups = Group1.class)
    @Past
    @CrossParameter
    @CrossParameter2
    @Pattern(regexp = "^\\d{11}$")
    public void testValida(){

    }

    @NotNull(message = "group1不能为空",groups = Group1.class)
    @NotEmpty(message = "111")
    @Past
    @CrossParameter
    @CrossParameter2
    @Future
    public void testValidaGroup1(){

    }

}
