package tpi.tusi

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import tpi.tusi.data.daos.UsuariosDao

@RunWith(AndroidJUnit4::class)
class PasswordRecoveryTest {
    @Test
    fun emailExist() = runBlocking(){
        val usuariosDao = UsuariosDao()
        val estado = usuariosDao.getIfUserExistByEmail("asdf@g.com")
        assert(estado, {"El email existe"})
    }
    @Test
    fun emailNoExist() = runBlocking(){
        val usuariosDao = UsuariosDao()
        val estado = usuariosDao.getIfUserExistByEmail("laguna@g.com")
        assert(!estado, { "El email no existe!" })
    }
}