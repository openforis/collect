package org.openforis.collect;

import org.openforis.collect.spring.MessageContextHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		MessageContextHolder bean = ctx.getBean(MessageContextHolder.class);
		System.out.println(bean.getMessage("validation.codeError"));
	}

}
