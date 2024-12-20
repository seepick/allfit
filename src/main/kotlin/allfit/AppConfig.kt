package allfit

import java.time.LocalDateTime

data class AppConfig(
    val isDevMode: Boolean,
    val mockClient: Boolean,
    val preSyncEnabled: Boolean,
    val syncEnabled: Boolean,
    val mockDb: Boolean,
    val mockDataStore: Boolean,
    val useLogFileAppender: Boolean,
    val dummyDate: LocalDateTime? = null,
) {

    companion object {
        val develop = AppConfig(
            isDevMode = true,
            mockClient = true,
            preSyncEnabled = false,
            syncEnabled = false,
//            mockDb = false,
//            mockDataStore = false,
            mockDb = false,
            mockDataStore = false,
//            dummyDate = null,
            dummyDate = LocalDateTime.parse("2023-05-26T14:23:21"),
            useLogFileAppender = false,
        )
        val prod = AppConfig(
            isDevMode = false,
            useLogFileAppender = true,
            preSyncEnabled = true,
            syncEnabled = true,
            mockClient = false,
            mockDb = false,
            mockDataStore = false,
        )
    }
}
