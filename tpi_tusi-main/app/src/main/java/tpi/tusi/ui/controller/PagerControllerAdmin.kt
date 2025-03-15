package tpi.tusi.ui.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tpi.tusi.ui.fragments.ListarCursosAdminFragment
import tpi.tusi.ui.fragments.ListarGestoresFragment

class PagerControllerAdmin(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    // Devuelve el fragmento según la posición del tab
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListarCursosAdminFragment() // Primer tab (Cursos)
            1 -> ListarGestoresFragment() // Segundo tab (Gestores)
            else -> ListarCursosAdminFragment()
        }
    }

    // Devuelve el número de tabs
    override fun getItemCount(): Int {
        return 2
    }

}