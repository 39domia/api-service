package com.service;

import com.service.api.controller.AuthenticationController;
import com.service.api.controller.CommonController;
import com.service.api.controller.UserController;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ApiServiceApplicationTests {

	@Autowired
	private AuthenticationController authenticationController;
	@Autowired
	private CommonController commonController;
	@Autowired
	private UserController userController;

	// Testing if Application Loads Correctly
	@Test
	public void contextLoads() {
		Assertions.assertThat(authenticationController).isNotNull();
		Assertions.assertThat(commonController).isNotNull();
		Assertions.assertThat(userController).isNotNull();
	}

}
