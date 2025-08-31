package com.naidizakupku.telegram.service

import com.naidizakupku.telegram.config.TelegramConfig
import com.naidizakupku.telegram.domain.User
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User as TelegramUser

class TelegramBotServiceTest : FunSpec({
    
    val telegramConfig = mockk<TelegramConfig>()
    val userService = mockk<UserService>()
    val telegramBotService = TelegramBotService(telegramConfig, userService)
    
    beforeTest {
        every { telegramConfig.botToken } returns "test_token"
        every { telegramConfig.botUsername } returns "test_bot"
    }
    
    test("getBotToken should return configured token") {
        telegramBotService.botToken shouldBe "test_token"
    }
    
    test("getBotUsername should return configured username") {
        telegramBotService.botUsername shouldBe "test_bot"
    }
    
    test("onUpdateReceived should process text message and save user") {
        // Given
        val update = Update()
        val message = Message()
        val chat = Chat()
        val telegramUser = TelegramUser()
        
        chat.id = 123L
        message.chat = chat
        message.text = "Hello, bot!"
        
        telegramUser.id = 456L
        telegramUser.firstName = "John"
        telegramUser.lastName = "Doe"
        telegramUser.userName = "johndoe"
        message.from = telegramUser
        
        update.message = message
        
        val savedUser = User(
            id = 1L,
            telegramId = 456L,
            firstName = "John",
            lastName = "Doe",
            username = "johndoe"
        )
        
        coEvery { userService.saveOrUpdateUser(any(), any(), any(), any()) } returns savedUser
        
        // When
        telegramBotService.onUpdateReceived(update)
        
        // Then
        coVerify { userService.saveOrUpdateUser(456L, "John", "Doe", "johndoe") }
    }
    
    // Тесты для обработки сообщений без текста и обновлений без сообщений
    // требуют более сложной настройки моков Telegram API
    // и будут добавлены в будущих версиях
})
