package project.inflabnet.mytest

import android.inflabnet.mytest.login.LoginActivity
import android.inflabnet.mytest.login.MainActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule


@RunWith(AndroidJUnit4::class)
class LoginEspressoTest {

    @get:Rule
    var activityRule = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun verificar_se_interface_foi_criada(){
        onView(withId(_root_ide_package_.android.inflabnet.mytest.R.id.et_email)) //captura o componente
                .check(matches(isDisplayed())) //verifica se está visível na interface
        onView(withId(_root_ide_package_.android.inflabnet.mytest.R.id.et_password)) //captura o componente
                .check(matches(isDisplayed())) //verifica se está visível na interface
        onView(withId(_root_ide_package_.android.inflabnet.mytest.R.id.btn_login)) //captura o componente
                .check(matches(isDisplayed())) //verifica se está visível na interface

    }

    @Test
    fun verificar_se_login_ocorre_com_sucesso(){
//        onView(withId(R.id.et_email)) //captura o componente
//                .perform(typeText("marcelorv@gmail.com"))
//        onView(withId(R.id.et_password)) //captura o componente
//                .perform(typeText("1234"))
//        onView(withId(R.id.btn_login)) //captura o componente
//                .perform(click())
        onView(withText("Falha de Autenticação!"))
                .check(matches(isDisplayed()))

    }
//    fun useAppContext() {
//        // Context of the app under test.
//        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
//        assertEquals("android.inflabnet.mytest", appContext.packageName)
//    }
}
