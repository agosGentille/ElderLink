package tpi.tusi.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import tpi.tusi.ui.fragments.LoginFragment;
import tpi.tusi.ui.fragments.RegistroPt1Fragment;
import tpi.tusi.ui.fragments.RegistroPt2Fragment;

public class AccesoPageAdapter extends FragmentStateAdapter {
    public AccesoPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new LoginFragment();
            case 1:
                return new RegistroPt1Fragment();
            default:
                return new LoginFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
