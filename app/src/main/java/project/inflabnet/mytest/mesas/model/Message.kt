package project.inflabnet.mytest.mesas.model
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Message { var userChat: String? = null
                var text: String? = null
                var timestamp: String? = null
                var self: Boolean? = false
                var hora: String? = null

    constructor() //empty for firebase

    constructor(userC:String,messageText: String, timestampChat: String, selfMessage: Boolean, horaChat:String){
        userChat = userC
        text = messageText
        timestamp = timestampChat
        self = selfMessage
        hora = horaChat
    }

}