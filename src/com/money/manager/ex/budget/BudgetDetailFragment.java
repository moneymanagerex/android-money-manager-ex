package com.money.manager.ex.budget;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.money.manager.ex.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IBudgetDetailCallbacks} interface
 * to handle interaction events.
 * Use the {@link BudgetDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetDetailFragment
        extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_BUDGET_YEAR_ID = "budgetYearId";
    private static final String ARG_PARAM2 = "param2";

    private long mBudgetYearId = Constants.NOT_SET;

    private IBudgetDetailCallbacks mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BudgetDetailFragment.
     */
    public static BudgetDetailFragment newInstance(long budgetYearId) {
        BudgetDetailFragment fragment = new BudgetDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_BUDGET_YEAR_ID, budgetYearId);
        fragment.setArguments(args);

        return fragment;
    }

    public BudgetDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_budget_detail, container, false);
//    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
