package com.catfish;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by A on 2017/4/12.
 */
public class HibernateControllerTest extends BaseControllerTest {

    @Test
    public void changePassword() throws Exception {
        Map<String,String> map=new HashMap<String, String>();
        String result = getForm("/hibernate/changePassword.do", map);
//        Assert.assertEquals("phone电话号码要求11位",result);
    }

}
