package com.example.messenger.Models

class ChatMessage(val id: String, val text: String, val fromId: String, val toId: String, val timesStamp: Long){
    constructor():this ("","","","",-1)
}