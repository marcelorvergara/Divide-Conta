package project.inflabnet.mytest.database.dao


import android.os.FileObserver.DELETE
import androidx.room.*
import project.inflabnet.mytest.database.model.Orcamento
import project.inflabnet.mytest.database.model.OrcamentoEMesa
import java.nio.channels.SelectableChannel

@Dao
interface OrcamentoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardar(orcamento: Orcamento)

    @Query("Select count(*) from Orcamento where mes = :Mes")
    fun getNumRows(Mes: String):Int

    @Query("DELETE FROM Orcamento")
    fun delete()

    @Query("Select * from  Orcamento")
    fun buscar (): Array<Orcamento>

    @Query("Select * from  Orcamento where valor = :valor")
    fun buscarOrc (valor: Int):Orcamento

    @Query("Select * from  Orcamento where mes = :Mes")
    fun buscarMes (Mes: String): Orcamento

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun atualiza(orcamento: Orcamento)

    @Transaction
    @Query ("Select * from Orcamento")
    fun buscaGeral(): List<OrcamentoEMesa>

    //busca id para inserir na tabela MesaOrc
    @Query ("Select id from Orcamento where mes = :Mes")
    fun buscarIdMes(Mes: String): Int
}