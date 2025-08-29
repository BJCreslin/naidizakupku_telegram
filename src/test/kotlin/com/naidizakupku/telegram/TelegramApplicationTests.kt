package com.naidizakupku.telegram

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class TelegramApplicationTests : FunSpec({
    
    test("context should load") {
        // Проверяем что контекст Spring загружается
        true shouldBe true
    }
})

