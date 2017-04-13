package com.catfish;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by A on 2017/4/12.
 */
public class Test1 {

    @Test
    public void tt() {
        List<String> list = new ArrayList();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        for (Iterator<String> it = list.iterator(); it.hasNext(); ) {
            String s = it.next();
            System.out.println(s);
            it.remove();
        }
        System.out.println(list.size());
    }


}
