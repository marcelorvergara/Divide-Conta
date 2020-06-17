package project.inflabnet.mytest

import android.inflabnet.mytest.mesas.model.Conta
import android.inflabnet.mytest.throwables.ContaValorException
import android.inflabnet.mytest.throwables.ItemNomeException
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test

class ContaClass {

    lateinit var conta: Conta

    //inicializar uma conta teste
    @Before
    fun setupConta(){
        conta = Conta("marcelorv", "cocacola", 15,"1231234","TABLE")
    }

    @Test
    fun verifica_se_eh_uma_instancia_da_conta(){

        assertTrue(conta is Conta)
    }

    @Test
    fun verifica_se_valor_eh_maior_que_zero(){
        try{
            conta = Conta("marcelorv", "cocacola", -12,"1231234","TABLE")
            assertTrue(false)
        }catch (e: ContaValorException){
            assertEquals(ContaValorException().message,
                    e.message)
        }

    }

    @Test fun verificar_nome_Item_mais_que_tres_char(){
        try{
            conta = Conta("marcelorv", "cocacola", 15,"1231234","TABLE")
            assertTrue(false)
        }catch (e: ItemNomeException){
            assertEquals(
                    ItemNomeException().message,
                    e.message
            )
        }

    }
}