package org.example.expert.domain.common.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class AdminLogAop {

	private final ObjectMapper objectMapper;

	public AdminLogAop(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Around(
		"execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
			"execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))"
	)
	public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {

		// HTTP 요청 객체를 가져온다.
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();

		HttpServletRequest request = servletRequestAttributes.getRequest();

		Long userId = (Long)request.getAttribute("userId");
		LocalDateTime requestTime = LocalDateTime.now();
		String requestUrl = request.getRequestURI();

		String requestBody = objectMapper.writeValueAsString(getRequestBody(joinPoint.getArgs()));

		log.info(
			"[Admin API Request] userId={}, requestTime={}, requestUrl={}, requestBody={}",
			userId, requestTime, requestUrl, requestBody
		);

		// 실제 메서드를 실행하고 반환값 저장
		Object result = joinPoint.proceed();

		String responseBody = objectMapper.writeValueAsString(result);

		log.info(
			"[Admin API Response] userId={}, requestTime={}, requestUrl={}, responseBody={}",
			userId, requestTime, requestUrl, responseBody
		);

		return result;
	}

	@AfterThrowing(
		pointcut =
			"execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
				"execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))",
		throwing = "e"
	)
	public void logAdminApiError(Exception e) {

		// HTTP 요청 객체를 가져온다.
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();

		HttpServletRequest request = servletRequestAttributes.getRequest();

		Long userId = (Long)request.getAttribute("userId");

		log.error(
			"[Admin API Error] userId={}, requestUrl={}, error={}",
			userId, request.getRequestURI(), e.getMessage(), e
		);
	}

	// @PathVariable(Long)은 제외하고 RequestBody 객체만 추출
	private Object getRequestBody(Object[] args) {
		for (Object arg : args) {
			if (!(arg instanceof Long)) {
				return arg;
			}
		}

		return null;
	}
}