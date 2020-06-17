package project.inflabnet.mytest.database.model

import androidx.room.Embedded
import androidx.room.Relation

//um orcamento tem varias mesas com seus gastos
class OrcamentoEMesa (
            @Embedded val orcamento: Orcamento,
            @Relation(
                parentColumn = "id", //id na tabela orçamento
                entityColumn = "id_orcamento" //id de orçamento na tabela MesaOrc
            ) val mesaorc: List<MesaOrc>
)