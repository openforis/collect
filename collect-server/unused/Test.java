package org.openforis.collect;

import org.openforis.collect.spring.SpringMessageSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml");
		SpringMessageSource bean = ctx.getBean(SpringMessageSource.class);
		System.out.println(bean.getMessage("validation.codeError"));
	}

}
