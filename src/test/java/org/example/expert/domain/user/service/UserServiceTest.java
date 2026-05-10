package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService userService;

	@Test
	void user_조회에_성공한다() {
		// given
		long userId = 1L;
		User user = new User("test@test.com", "password", UserRole.USER);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserResponse response = userService.getUser(userId);

		// then
		assertNotNull(response);
		assertEquals("test@test.com", response.getEmail());
	}

	@Test
	void user_조회에_실패하면_예외가_발생한다() {
		// given
		long userId = 1L;

		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userService.getUser(userId)
		);

		assertEquals("User not found", exception.getMessage());
	}

	@Test
	void password를_정상적으로_변경한다() {
		// given
		long userId = 1L;
		User user = new User("test@test.com", "oldEncodedPassword", UserRole.USER);

		UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword");

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
		given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
		given(passwordEncoder.encode(request.getNewPassword())).willReturn("newEncodedPassword");

		// when
		userService.changePassword(userId, request);

		// then
		assertEquals("newEncodedPassword", user.getPassword());
	}

	@Test
	void NewPassword가_oldPassword와_같으면_예외가_발생한다() {
		// given
		long userId = 1L;
		User user = new User("test@test.com", "encodedPassword", UserRole.USER);

		UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword");

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(true);

		// when & then
		InvalidRequestException exception = assertThrows(InvalidRequestException.class,
			() -> userService.changePassword(userId, request)
		);

		assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
	}
}