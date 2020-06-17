package project.inflabnet.mytest.database.database


import androidx.room.Database
import androidx.room.RoomDatabase
import project.inflabnet.mytest.database.dao.MesaDAO
import project.inflabnet.mytest.database.dao.OrcamentoDAO
import project.inflabnet.mytest.database.model.MesaOrc
import project.inflabnet.mytest.database.model.Orcamento

//anotação com relação de entidades(tabelas) que compõe a base
@Database(
    entities = arrayOf(
        Orcamento::class,
        MesaOrc::class
    ),
    //para notificar mudanças da base de dados do dispositivo
    version = 7
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun orcamentoDAO(): OrcamentoDAO
    abstract fun mesaDAO(): MesaDAO

}