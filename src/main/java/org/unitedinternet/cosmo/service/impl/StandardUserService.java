package org.unitedinternet.cosmo.service.impl;

import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

import carldav.entity.CollectionItem;
import carldav.entity.User;
import carldav.repository.UserRepository;

public class StandardUserService implements UserService {

	private final ContentService contentService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public StandardUserService(
			ContentService contentService,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.contentService = Objects.requireNonNull(contentService, "contentService is null");
		this.userRepository = Objects.requireNonNull(userRepository, "userRepository is null");
		this.passwordEncoder = Objects.requireNonNull(passwordEncoder, "passwordEncoder is null");
	}

	public User createUser(User user) {
		user.setPassword(digestPassword(user.getPassword()));

		var newUser = userRepository.save(user);

		var calendar = new CollectionItem();
		calendar.setOwnerId(user.getId());
		calendar.setName("calendar");
		calendar.setDisplayName("%s's calendar".formatted(user.getEmail()));

		var homeCollection = contentService.createRootItem(newUser);
		contentService.createCollection(homeCollection, calendar);

		var addressbook = new CollectionItem();
		addressbook.setOwnerId(user.getId());
		addressbook.setName("contacts");
		addressbook.setDisplayName("%s's contacts".formatted(user.getEmail()));

		contentService.createCollection(homeCollection, addressbook);

		return newUser;
	}

	private String digestPassword(String password) {
		if (password == null) {
			return null;
		}
		return passwordEncoder.encode(password);
	}
}
