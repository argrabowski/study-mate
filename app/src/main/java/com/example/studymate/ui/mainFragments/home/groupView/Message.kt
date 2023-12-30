package com.example.studymate.ui.mainFragments.home.groupView

import java.util.*

class Message(
    val sender: String?,
    val message: String?,
    private val messageId: UUID = UUID.randomUUID()
){
    override fun toString(): String {
        return "Message(sender=$sender, message=$message, messageId=$messageId)"
    }
}