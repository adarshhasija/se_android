package com.starsearth.one.runnable

import android.os.Bundle
import com.starsearth.one.database.Firebase
import com.starsearth.one.domain.Task

class ResultSaveRunnable internal constructor(bundle: Bundle?) : Runnable {

    private var taskId: Int
    private var taskTypeLong: Long
    private var timeTakenMillis: Long

    private var itemsAttempted: Int
    private var itemsCorrect: Int

    private var charactersCorrect: Int
    private var totalCharactersAttempted: Int
    private var wordsCorrect: Int
    private var totalWordsFinished: Int

    init {
        taskId = bundle?.getInt("taskId", -1)!!
        taskTypeLong = bundle?.getLong("taskTypeLong", 0)!!
        timeTakenMillis = bundle?.getLong("timeTakenMillis", 0)
        itemsCorrect = bundle?.getInt("itemsCorrect", 0)
        itemsAttempted = bundle?.getInt("itemsAttempted", 0)
        charactersCorrect = bundle?.getInt("charactersCorrect", 0)
        totalCharactersAttempted = bundle?.getInt("totalCharactersAttempted", 0)
        wordsCorrect = bundle?.getInt("wordsCorrect", 0)
        totalWordsFinished = bundle?.getInt("totalWordsFinished", 0)
    }

    override fun run() {
        val firebase = Firebase("results")
        val type = Task.Type.fromInt(taskTypeLong)
        if (type == Task.Type.TYPING) {
            firebase.writeNewResultTyping(charactersCorrect, totalCharactersAttempted, wordsCorrect, totalWordsFinished, timeTakenMillis, taskId)
        } else {
            firebase.writeNewResultGestures(itemsAttempted, itemsCorrect, timeTakenMillis, taskId)
        }
    }
}
