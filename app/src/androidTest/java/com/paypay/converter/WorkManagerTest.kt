/*
 * *
 *  * Created by Okechukwu Agufuobi on 13/12/2021, 3:06 PM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 13/12/2021, 3:06 PM
 *
 */

package com.paypay.converter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result.success
import androidx.work.testing.TestListenableWorkerBuilder
import com.paypay.converter.workers.ConverterWorker
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors


@RunWith(AndroidJUnit4::class)
class WorkManagerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun testConvertWorker() {
        val worker = TestListenableWorkerBuilder<ConverterWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, `is`(success()))
        }
    }
}