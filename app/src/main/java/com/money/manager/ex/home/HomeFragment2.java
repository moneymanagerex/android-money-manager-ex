package com.money.manager.ex.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.Lazy;

public class HomeFragment2
            extends Fragment {

    @Inject
    Lazy<InfoRepositorySql> infoRepositorySqlLazy;

    private CurrencyService mCurrencyService;
    private boolean mHideReconciled;
    private FloatingActionButton mFloatingActionButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);

        mCurrencyService = new CurrencyService(getActivity().getApplicationContext());

        refreshSettings();

        // The fragment is using a custom option in the actionbar menu.
        // setHasOptionsMenu(true);

        // restore number input binaryDialog reference, if any
        if (savedInstanceState != null) {
            // restore number input binaryDialog reference, if any
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) return null;

        // inflate layout
        View view = inflater.inflate(R.layout.home_fragment2, container, false);


//        mFloatingActionButton = view.findViewById(R.id.fab);
        if (mFloatingActionButton != null) {
            mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);
                    intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "HomeFragment.java");
                    intent.setAction(Intent.ACTION_INSERT);
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
//        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void refreshSettings() {
        AppSettings settings = new AppSettings(getActivity());
        mHideReconciled = settings.getLookAndFeelSettings().getHideReconciledAmounts();

    }

}
