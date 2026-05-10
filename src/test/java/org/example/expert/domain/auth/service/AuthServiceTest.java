package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwtUtil;

	@InjectMocks
	private AuthService authService;

	@Test
	void Signup에_성공한다() {
		// given
		SignupRequest request = new SignupRequest("test@test.com", "password123", "USER");
		User savedUser = new User(request.getEmail(), "encodedPassword", UserRole.USER);
		ReflectionTestUtils.setField(savedUser, "id", 1L);

		given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
		given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
		given(userRepository.save(any(User.class))).willReturn(savedUser);
		given(jwtUtil.createToken(1L, request.getEmail(), UserRole.USER)).willReturn("Bearer signup-token");

		// when
		SignupResponse response = authService.signup(request);

		// then
		assertNotNull(response);
		assertEquals("Bearer signup-token", response.getBearerToken());
	}

	@Test
	void 로그인에_성공한다() {
		// given
		SigninRequest request = new SigninRequest("test@test.com", "password123");
		User user = new User("test@test.com", "encodedPassword", UserRole.USER);
		ReflectionTestUtils.setField(user, "id", 1L);

		given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
		given(jwtUtil.createToken(1L, user.getEmail(), user.getUserRole())).willReturn("Bearer signin-token");

		// when
		SigninResponse response = authService.signin(request);

		// then
		assertNotNull(response);
		assertEquals("Bearer signin-token", response.getBearerToken());
	}
}