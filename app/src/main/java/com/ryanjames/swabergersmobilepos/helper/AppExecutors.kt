package com.ryanjames.swabergersmobilepos.helper

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

object AppExecutors {

    val diskIo: Executor = Executors.newSingleThreadExecutor()


}