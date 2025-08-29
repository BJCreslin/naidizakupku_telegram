package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.domain.User
import com.naidizakupku.telegram.repository.UserRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class UserServiceTest : FunSpec({

    val userRepository = mockk<UserRepository>()
    val userService = UserService(userRepository)

    test("should create user successfully") {
        runBlocking {
            // Given
            val telegramId = 123456789L
            val username = "testuser"
            val firstName = "Test"
            val lastName = "User"

            val expectedUser = User(
                id = 1L,
                telegramId = telegramId,
            )

            coEvery { userRepository.existsByTelegramId(telegramId) } returns false
            coEvery { userRepository.save(any()) } returns expectedUser

            // When
            val result = userService.createUser(telegramId)

            // Then
            result shouldNotBe null
            result.telegramId shouldBe telegramId
        }
    }

    test("should find user by telegram id") {
        runBlocking {
            // Given
            val telegramId = 123456789L
            val expectedUser = User(
                id = 1L,
                telegramId = telegramId,
            )

            coEvery { userRepository.findByTelegramId(telegramId) } returns expectedUser

            // When
            val result = userService.findByTelegramId(telegramId)

            // Then
            result shouldNotBe null
            result?.telegramId shouldBe telegramId
        }
    }
})

