package project.inflabnet.mytest.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Orcamento (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var mes: String,
    var valor: Int
){
    override fun toString(): String {
        var mes = "Id ${id}, Mes ${mes}, Valor ${valor}"
        return mes
    }
}