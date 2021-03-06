package com.skywomantech.app.symptommanagement.admin;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.admin.Medication.AdminMedicationListActivity;
import com.skywomantech.app.symptommanagement.admin.Patient.AdminPatientListActivity;
import com.skywomantech.app.symptommanagement.admin.Physician.AdminPhysicianListActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A placeholder fragment containing a simple view.
 */
public class AdminMainFragment extends Fragment {

    public AdminMainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin_main, container, false);
        ButterKnife.inject(this, rootView);
        setRetainInstance(true); // save fragment across config changes
        return rootView;
    }

    @OnClick(R.id.edit_patients_button)
    public void addEditPatients() {
        startActivity(new Intent(getActivity(), AdminPatientListActivity.class));
    }

    @OnClick(R.id.edit_physicians_button)
    public void addEditPhysicians() {
        startActivity(new Intent(getActivity(), AdminPhysicianListActivity.class));
    }

/*    There is no real need for this in the admin side anymore but it could be put back in later
    @OnClick(R.id.edit_medications_button)
    public void addEditMedications() {
        startActivity(new Intent(getActivity(), AdminMedicationListActivity.class));
    }*/

}
