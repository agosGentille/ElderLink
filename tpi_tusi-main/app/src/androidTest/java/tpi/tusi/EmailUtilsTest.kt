package tpi.tusi

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import tpi.tusi.data.database.DataDB
import tpi.tusi.ui.entities.Email
import tpi.tusi.ui.utils.EmailUtils

@RunWith(AndroidJUnit4::class)
class EmailUtilsTest {
    @Test
    fun emailTesting(){
        val emailUtils = EmailUtils(
            DataDB.usernameGmail,
            DataDB.passwordGmail
        )
        val email = Email(
            recipient = "lamofa@gmail.com",
            subject = "testiando clase email",
            message = "esto funciona?"
        )
        assert(emailUtils.sendEmail(email), { "todo OK" })
    }

}