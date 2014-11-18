package com.skywomantech.app.symptommanagement.physician;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.client.CallableTask;
import com.skywomantech.app.symptommanagement.client.SymptomManagementApi;
import com.skywomantech.app.symptommanagement.client.SymptomManagementService;
import com.skywomantech.app.symptommanagement.client.TaskCallback;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.StatusLog;
import com.skywomantech.app.symptommanagement.data.UserCredential;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.concurrent.Callable;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PhysicianPatientDetailFragment extends Fragment {

    public final static String LOG_TAG = PhysicianPatientDetailFragment.class.getSimpleName();

    public interface Callbacks {
        public void onPatientContacted(String patientId, StatusLog statusLog);
        public void onPatientFound(Patient patient);
    }

    public static final String PATIENT_ID_KEY = "patient_id";
    public static final String PHYSICIAN_ID_KEY = "physician_id";

    private String mPatientId;
    private Patient mPatient;

    @InjectView(R.id.physician_patient_detail_name)
    TextView mNameView;

    @InjectView(R.id.physician_patient_detail_birthdate)
    TextView mBDView;

    @InjectView(R.id.patient_medical_id)
    TextView mRecordId;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicianPatientDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(PATIENT_ID_KEY)) {
            mPatientId = getArguments().getString(PATIENT_ID_KEY);
        }
        setHasOptionsMenu(true);
        setRetainInstance(true);  // save the fragment state with rotations
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_physician_patient_detail, container, false);
        ButterKnife.inject(this, rootView);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PhysicianPatientDetailFragment.PATIENT_ID_KEY)) {
                mPatientId =
                        savedInstanceState.getString(PhysicianPatientDetailFragment.PATIENT_ID_KEY);
            }
        }
        mPatient = getPatientFromCloud();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arguments = getArguments();
        if (mPatientId == null &&
                arguments != null
                && arguments.containsKey(PhysicianPatientDetailFragment.PATIENT_ID_KEY) ) {
            mPatientId = arguments.getString(PhysicianPatientDetailFragment.PATIENT_ID_KEY);
        }
    }

    //TODO: move this to the activity... not sure it fits this fragment any more!
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.physician_patient_contact_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_status) {
            if (mPatient != null) {
                Log.d(LOG_TAG, "Adding a Physician Status Log");
                addPhysicianStatusLog(mPatientId);
                SymptomManagementSyncAdapter.syncImmediately(getActivity());
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPhysicianStatusLog(final String patientId) {
        AlertDialog alert = new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Patient Contact")
                .setMessage("Did you contact Patient?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addPatientContactStatus(patientId);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                }).create();
        alert.show();
    }

    private void addPatientContactStatus(final String patientId) {
        if (!LoginUtility.isLoggedIn(getActivity())
                || LoginUtility.getUserRole(getActivity()) != UserCredential.UserRole.PHYSICIAN) {
            Log.d(LOG_TAG, "This user isn't a physician why are they here?");
            return;
        }
        String contactNote = "Patient Contacted by Physician";
        StatusLog statusLog = new StatusLog();
        statusLog.setNote(contactNote);
        statusLog.setCreated(System.currentTimeMillis());
        // have the activity save the physician data
        ((Callbacks) getActivity()).onPatientContacted(mPatientId, statusLog);

    }

    private Patient getPatientFromCloud() {

        if (mPatientId == null) return null;
        final SymptomManagementApi svc = SymptomManagementService.getService();
        if (svc != null) {
            CallableTask.invoke(new Callable<Patient>() {

                @Override
                public Patient call() throws Exception {
                    Log.d(LOG_TAG, "getting Patient ID : " + mPatientId);
                    return svc.getPatient(mPatientId);
                }
            }, new TaskCallback<Patient>() {

                @Override
                public void success(Patient result) {
                    mPatient = result;
                    Log.d(LOG_TAG, "got the Patient!" + mPatient.toDebugString());
                    if (mPatient != null) {
                        // set the views with the patient data
                        mNameView.setText(mPatient.getName());
                        mBDView.setText(mPatient.getBirthdate());
                        mRecordId.setText(mPatient.getId());
                        ((Callbacks) getActivity()).onPatientFound(mPatient);
                    }
                }

                @Override
                public void error(Exception e) {
                    Toast.makeText(getActivity(),
                            "Unable to fetch the Patient data. " +
                                    "Please check Internet connection and try again.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
        return null;
    }
}
