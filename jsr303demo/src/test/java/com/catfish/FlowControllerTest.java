package com.catfish;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by A on 2017/4/7.
 */

public class FlowControllerTest extends BaseControllerTest {

    /**
     * 测试自带基本注解
     *
     * @throws Exception
     */
    @Test
    public void testValidaGeneric() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String result = getForm("/flow/testValidaGeneric.do", map);
        Assert.assertEquals("test用户id不能为null", result);
    }

    /**
     * 测试自带基本注解,group的使用
     *
     * @throws Exception
     */
    @Test
    public void testValidaGeneric2() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String result = getForm("/flow/testValidaGeneric2.do", map);
        Assert.assertEquals("group1不能为空", result);
    }

    /**
     * 测试校验组合
     *
     * @throws Exception
     */
    @Test
    public void testValidaCompose() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("price", "1");
        String result = getForm("/flow/testValidaCompose.do", map);
        System.out.println(result);
        Assert.assertEquals("价格最小不能小于8000", result);
    }

    @Test
    public void testValidaCompose2() throws Exception {
        for (int i=0;i<2;i++){
            Map<String, String> map = new HashMap<String, String>();
            String result = getForm("/flow/testValidaCompose2.do", map);
            System.out.println(result);
            Assert.assertEquals("时间需要是一个过去的时间", result);
        }
    }

    /**
     * 测试bean的校验
     *
     * @throws Exception
     */
    @Test
    public void testBean() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "1");
        map.put("phone", "11");
        String result = getForm("/flow/testBean.do", map);
        Assert.assertEquals("id用户id不能为null", result);
    }

  /*  @Test
    public void testBeanGroup() throws Exception {
        Map<String,String> map=new HashMap<String, String>();
        map.put("phone", "11");
        map.put("age","1");
        map.put("id","1");
        String result = getForm("/flow/testBeanGroup.do", map);
    }*/


    @Test
    public void testCallback() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String result = getForm("/flow/callback.do", map);
        System.out.println(result);
//        Assert.assertEquals("phone电话号码要求11位",result);
    }

    @Test
    public void testChangePassword2() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String result = getForm("/flow/changePassword2.do", map);
        System.out.println(result);
        Assert.assertEquals("密码错误",result);
    }

    @Test
    public void testWhen() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String result = getForm("/flow/when.do", map);
        System.out.println(result);
        Assert.assertEquals("用户id不能为null",result);
    }


}
