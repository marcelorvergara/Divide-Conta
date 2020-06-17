package project.inflabnet.mytest.mesas.model

import project.inflabnet.mytest.throwables.ContaValorException
import project.inflabnet.mytest.throwables.ItemNomeException
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Conta {
    var quem: String? = null
    var oque: String? = null
    var quanto: Int? = null
    var timestamp: String? = null
    var mesa: String? = null

    constructor() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    constructor(username: String?,
                item: String?,
                valor: Int?,
                timestamp: String, mesa: String) {
        if (valor != null) {
            if(valor < 0) throw  ContaValorException()
        }

        if (item != null) {
            if (item.length < 3) throw ItemNomeException()
        }
        this.quem = username
        this.oque = item
        this.quanto = valor
        this.timestamp = timestamp
        this.mesa = mesa
    }

//    init {
//        if (quanto!! < 0) throw ContaValorException()
//        if (oque?.length!! < 3) throw  ItemNomeException()
//    }
}