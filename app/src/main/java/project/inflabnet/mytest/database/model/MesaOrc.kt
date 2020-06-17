package project.inflabnet.mytest.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MesaOrc (
    var id_orcamento: Int, //todo gasto em uma mesa está associado a um Orçamento, mas o transaction não funciona
    var nome_mesa: String,
    var gasto: Int,
    var mesMesa: String,
    @PrimaryKey(autoGenerate = true)
    var id_mesa: Int? = null
){
    override fun toString(): String {
        return "Mês ${mesMesa}, Mesa ${nome_mesa}, Gastou R$ ${gasto}"
    }
}
