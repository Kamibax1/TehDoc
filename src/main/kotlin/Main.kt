import kotlinx.coroutines.*
import java.io.PrintStream
import java.nio.charset.StandardCharsets

fun main() = runBlocking {

    System.setOut(PrintStream(System.out, true, StandardCharsets.UTF_8))

    val exceptionHandler = CoroutineExceptionHandler { context, exception ->
        println("[HANDLER] Неперехваченное исключение: $exception")
    }

    val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val service = ConcurrentService(serviceScope)

    println("--- Запуск фоновой задачи ---")
    val backgroundJob = service.startBackgroundTask(exceptionHandler)
    delay(100)

    println("--- Запуск 15 конкурентных инкрементов ---")
    val numIncrements = 15
    val jobs = List(numIncrements) { index ->

        launch(Dispatchers.Default) {
            val result = service.incrementCounter()

            // 3. Обработка Result
            result.fold(
                onSuccess = { newCount ->
                    println("Инкремент #$index успешен. Новое значение: $newCount")
                },
                onFailure = { e ->
                    println("Инкремент #$index провален. Ошибка: ${e.message}")
                }
            )
        }
    }

    jobs.joinAll()

    println("--- Проверка итогового состояния ---")
    val finalCount = service.getCounter()
    println("Итоговое значение счетчика: $finalCount")

    println("Ожидаем критической ошибки в фоновой задаче (3 секунды)...")
    delay(3000)

    println("--- Завершение сервиса ---")
    service.close()
}