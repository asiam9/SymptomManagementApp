package com.skywomantech.app.symptommanagement.patient;

import android.app.ActionBar;
import android.app.Fragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.Medication;
import com.skywomantech.app.symptommanagement.data.MedicationLog;
import com.skywomantech.app.symptommanagement.data.PatientCPContract;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.MedLogEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import java.util.Collection;
import java.util.HashSet;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This fragment displays the patient prescription list and asks if the patient has taken
 * any of the medications
 *
 * It lets the main activity know when a medication log has been added
 *
 * Adds the check-in id to the log so we know the data is connected to a check-in and which one
 *
 */
public class PatientMedicationLogFragment extends Fragment {

    public final static String LOG_TAG = PatientMedicationLogFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "patient_med_log_fragment";

    // tell the main activity that a medication log has been entered so it can do its processing
    // if needed
    public interface Callbacks {
        public boolean onMedicationLogComplete();
    }

    MedicationLogListAdapter mAdapter;
    private Collection<MedicationLog> medicationLogs;
    MedicationLog[] mLogList;
    // if this occurs during a check-in this id will be set the a true check-in id
    private long mCheckInId = 0L;

    @InjectView(R.id.patient_medication_check_list)  ListView mLogListView;

    public PatientMedicationLogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);  // save the fragment state with rotations

        // we want the action bar up to display on this one
        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_medication_log, container, false);
        ButterKnife.inject(this, rootView);
        return rootView;
    }

    // load a list of empty log records for the patient to fill
    @Override
    public void onResume() {
        super.onResume();
        // get the checkin Id to use for the logs
        mCheckInId = LoginUtility.getCheckInLogId(getActivity());
        // create a list of empty logs that the patient can enter dates into
        loadMedicationLogList();
    }

    @Override
    public void onPause() {
        super.onPause();
        // we are leaving this screen and this ALWAYS means that
        // there is no check-in process happening now
        LoginUtility.setCheckin(getActivity(), false);
    }

    private void loadMedicationLogList() {
        // make a blank list of possible entries for the patient to fill in
        Collection<Medication> prescriptions =
                PatientDataManager.getPrescriptionsFromCP(getActivity(),
                LoginUtility.getLoginId(getActivity()));
        if (mLogList == null) {
            createEmptyLogsList(prescriptions);
        }
        mAdapter = new MedicationLogListAdapter(getActivity(), mLogList);
        mLogListView.setAdapter(mAdapter);
    }

    /**
     * creates an list of prescriptions to be used by list adapter
     * The time taken is empty until the user inputs it
     *
     * @param medications
     */
    private void createEmptyLogsList(Collection<Medication> medications) {
        Log.d(LOG_TAG,"Setting all medication logs with a Check-in id of : "
                + Long.toString(mCheckInId));
        medicationLogs = new HashSet<MedicationLog>();
        for(Medication m: medications) {
            MedicationLog ml = new MedicationLog();
            ml.setMed(m);
            ml.setCheckinId(mCheckInId); // make sure we have a check-in id!
            medicationLogs.add(ml);
        }
        mLogList =  medicationLogs.toArray(new MedicationLog[medicationLogs.size()]);
    }

    /**
     * Main activity pushes this information to this fragment when the user has
     * entered a time taken for the prescription
     *
     * @param msTime  time entered by the patient
     * @param position where in the list of prescriptions this information goes
     */
    public void updateMedicationLogTimeTaken(long msTime, int position) {
        mLogList[position].setTaken(msTime);

        // save this one to the local storage
        String mPatientId = LoginUtility.getLoginId(getActivity());
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, mLogList[position]);
        Log.d(LOG_TAG, "Saving this Med Log : " + mLogList[position].toString());
        Uri uri = getActivity().getContentResolver().insert(MedLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Medication Log Insert Failed.");
        } else {
            ((Callbacks) getActivity()).onMedicationLogComplete();
        }
        // try to push this to the server so the doctor can view it
        SymptomManagementSyncAdapter.syncImmediately(getActivity());
        // list update
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

}
