package tpi.tusi.ui.controller

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tpi.tusi.ui.fragments.ListarCursosEstudianteFragment
import tpi.tusi.ui.fragments.ListarCursosGestorFragment
import tpi.tusi.ui.fragments.ListarMisCursosGestorFragment

class PagerControllerGestor(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    // Devuelve el fragmento según la posición del tab
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListarCursosGestorFragment()
            1 ->  ListarMisCursosGestorFragment()
            else -> ListarCursosGestorFragment()
        }
    }

    // Devuelve el número de tabs
    override fun getItemCount(): Int {
        return 2
    }

}