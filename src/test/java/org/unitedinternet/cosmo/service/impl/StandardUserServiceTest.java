package org.unitedinternet.cosmo.service.impl;

import carldav.CarldavApplication;
import carldav.entity.User;
import carldav.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { CarldavApplication.class })
@Transactional
@Rollback
class StandardUserServiceTest {

	@Autowired
	private UserService service;

	@Autowired
	private JdbcAggregateOperations template;

	@Autowired
	private CollectionRepository collectionRepository;

	@Test
	void testCreateUser() {
		var user = new User();
		user.setEmail("user@localhost");
		user.setPassword("somepassword");

		var createdUser = service.createUser(user);

		assertThat(template.findById(createdUser.getId(), User.class)).isNotNull();
		assertThat(createdUser.getPassword()).startsWith("{bcrypt}");
		assertThat(collectionRepository.findByOwnerEmail(createdUser.getEmail()))
				.extracting("displayName")
				.containsExactlyInAnyOrder("calendarDisplayName", "homeCollection", "contactDisplayName");
	}
}
