package com.example.imageresizeupscale

import com.example.imageresizeupscale.data.DataLayerFunctions
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun isPcAvailable_testMethod_assertTrue() {
        val isAvailable = DataLayerFunctions().testPcConnection("192.168.20.11")
        assertTrue("Network device available", isAvailable)
    }

    @Test
    fun filterBadCharsInFileName_testMethod_assertTrue() {
        val correctlyFilteredFileName = "dshbrguiserearg"
        val fileName = "dsh/brg|\\?*<\":>+[]/'uiser/earg."
        val filteredName = DataLayerFunctions().filterFileNameString(fileName)
        assertTrue("File name filtered correctly", filteredName == correctlyFilteredFileName)
    }
}