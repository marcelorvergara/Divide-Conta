package project.inflabnet.mytest.mesas.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Mesa {
    lateinit var nameMesa: String
    var proprietarioMesa: String? = null
    var timestamp: String? = null
    var pin:Int? = null
    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    constructor(mesa: String, user: String?,timestamp: String,senha:Int) {
        this.nameMesa = mesa
        this.proprietarioMesa = user
        this.timestamp = timestamp
        this.pin = senha
    }

    override fun toString(): String {
        return nameMesa
    }
}