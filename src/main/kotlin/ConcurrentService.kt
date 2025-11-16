import kotlinx.coroutines.*
import kotlinx.coroutines.sync.*

class ConcurrentService (
    private val scope: CoroutineScope,
    initialValue: Int = 0
){
    private val mutex = Mutex()
    private var counter: Int = initialValue

    suspend fun incrementCounter(): Result<Int> = runCatching {
        // Защита доступа к 'counter' с помощью Mutex
        mutex.withLock {
            delay(10) // Имитация работы с I/O или БД
            counter += 1
            if (counter % 10 == 0) {
                // Имитация ошибки, которая будет перехвачена runCatching
                throw IllegalStateException("Счетчик достиг кратного 10, имитация ошибки!")
            }
            return@withLock counter
        }
    }

    suspend fun getCounter(): Int = mutex.withLock {
        return@withLock counter
    }

    fun startBackgroundTask(handler: CoroutineExceptionHandler): Job = scope.launch(handler) {
        var runCount = 0
        while (isActive) { // Проверка на отмену
            delay(500)
            runCount++
            println("Фоновая задача: отработано $runCount раз.")

            if (runCount == 5) {
                // Вызовет CoroutineExceptionHandler, но не отменит другие корутины благодаря SupervisorJob
                throw RuntimeException("Критическая ошибка в фоновом процессе!")
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
