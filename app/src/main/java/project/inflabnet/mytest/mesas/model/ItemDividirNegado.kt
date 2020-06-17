package project.inflabnet.mytest.mesas.model

class ItemDividirNegado {var userOrigem: String? = null
                        var userDestino: String?= null
                        var itemAdividir: String?= null
                        var itemValor: String?= null
                        var mesaIAD: String?= null
                        var status: String?= null
                        var id: String? = null

    constructor() {
        // Default constructor
    }

    constructor(userOrigem: String?, userDestino: String?, itemAdividir: String?, itemValor: String, mesaIAD: String,status: String, id: String) {
        if (userOrigem != null) {
            this.userOrigem = userOrigem
        }
        this.userDestino = userDestino.toString()
        this.itemAdividir = itemAdividir.toString()
        this.itemValor = itemValor
        this.mesaIAD = mesaIAD
        this.status = status
        this.id = id
    }
}