package project.inflabnet.mytest

import android.content.Context
import android.inflabnet.mytest.database.dao.OrcamentoDAO
import android.inflabnet.mytest.database.database.AppDatabase
import android.inflabnet.mytest.database.model.Orcamento
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */


@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTest {
    private lateinit var db: AppDatabase
    private  lateinit var orcamentoDao: OrcamentoDAO
    @Before
    fun setup_db() {

        var context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        orcamentoDao = db.orcamentoDAO()
    }

    // @Entity Orçamento
    // @DAO
    // @Database AppDatabase --> retornam instancias de DAO

    @After
    fun closeDB(){
        db.close()
    }

    @Test
    fun verifica_se_ocamento_esta_ok(){
        //Instancia de Orçamento a ser armazenado
        val orcamento = Orcamento(1,1,100)

        orcamentoDao.guardar(orcamento)
        //AppDatabase.OrcamentoDAO.select(Orcamento.valor)
        var orcamento_result = orcamentoDao.buscar()
        //assertvia
        assertTrue(orcamento_result is Orcamento)
        assertEquals(orcamento_result.valor,orcamento.valor)
    }

    @Test
     fun verifica_se_orcamento_eh_atualizado(){
         var orcamento = Orcamento(1,12,700)

         orcamentoDao.guardar(orcamento)
         var orcamento2 = orcamentoDao.buscarMes(12)
         orcamento2.valor = 1500
         orcamentoDao.atualiza(orcamento2)
         var orcamento_result = orcamentoDao.buscarOrc(1500)

         assertEquals(orcamento2.valor,orcamento_result.valor)
     }

        // Context of the app under test.
        //val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        //assertEquals("android.inflabnet.mytest", appContext.packageName)

}
