package org.smart4j.chapter2.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.smart4j.chapter2.model.Customer;
import org.smart4j.chapter2.service.CustomerService;

import java.util.Collections;
import java.util.List;

/**
 * CustomerService单元测试
 */

public class CustomerServiceTest {

    private final CustomerService customerService;


    public CustomerServiceTest() {
        customerService = new CustomerService();
    }

    @Before
    public void init() {
        //TODO 初始化数据库
    }

    @Test
    public void getCustomerListTest() throws Exception {
        List<Customer> list = customerService.getCustomerList();
        System.out.println(list.size());
        list.forEach(item -> System.out.println(item));
    }


}
