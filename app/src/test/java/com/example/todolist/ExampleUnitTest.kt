package com.example.todolist

import org.junit.Assert.assertEquals
import org.junit.Test

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

//    @Test
//    fun check_taskListEntityAreNotEqual() {
//        val list1 = TaskListEntity(id = 1, listName = "Test").apply {
//            previewItems = emptyList()
//            checked = true
//        }
//        val list2 = TaskListEntity(id = 1, listName = "Test").apply {
//            previewItems = emptyList()
//            checked = false
//        }
//
//        assertNotEquals(true, list1 == list2)
//    }
}