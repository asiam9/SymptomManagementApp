package com.skywomantech.app.symptommanagement.patient;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skywomantech.app.symptommanagement.LoginUtility;
import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.PainLog;
import com.skywomantech.app.symptommanagement.data.PatientCPContract.PainLogEntry;
import com.skywomantech.app.symptommanagement.data.PatientCPcvHelper;
import com.skywomantech.app.symptommanagement.sync.SymptomManagementSyncAdapter;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * This class manages the screen for the pain level and eating ability questions
 *
 * There are a few check-in processing things that need to be done here
 *
 */
public class PatientPainLogFragment extends Fragment {

    public final static String LOG_TAG = PatientPainLogFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "patient_pain_log_fragment";

    // tells the main activity that the OK button was pressed and the log was saved
    // in this case there are check-in processing that needs to be done by the activity
    public interface Callbacks {
        public boolean onPainLogComplete(long checkinId);
    }

    private PainLog mLog;
    private String mPatientId;

    public PatientPainLogFragment() {
        mLog = new PainLog();
        mLog.setSeverity(PainLog.Severity.NOT_DEFINED);
        mLog.setEating(PainLog.Eating.NOT_DEFINED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // want the up navigation for this one
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pain_log, container, false);
        ButterKnife.inject(this, rootView);
        this.setRetainInstance(true);  // save the fragment state with rotations
        return rootView;
    }

    /**
     * process the radio buttons for the pain severity level choices
     * @param v
     */
    @OnClick({R.id.well_controlled_button, R.id.moderate_button, R.id.severe_button})
    public void onSeverityRadioGroup(View v) {
        switch (v.getId()) {
            case R.id.well_controlled_button:
                mLog.setSeverity(PainLog.Severity.WELL_CONTROLLED);
                break;
            case R.id.moderate_button:
                mLog.setSeverity(PainLog.Severity.MODERATE);
                break;
            case R.id.severe_button:
                mLog.setSeverity(PainLog.Severity.SEVERE);
                break;
        }
    }

    /**
     * process the eating ability radio buttons
     *
     * @param v
     */
    @OnClick({R.id.eating_ok_button, R.id.eating_some_button, R.id.not_eating_button})
    public void onEatingRadioGroup(View v) {
        switch (v.getId()) {
            case R.id.eating_ok_button:
                mLog.setEating(PainLog.Eating.EATING);
                break;
            case R.id.eating_some_button:
                mLog.setEating(PainLog.Eating.SOME_EATING);
                break;
            case R.id.not_eating_button:
                mLog.setEating(PainLog.Eating.NOT_EATING);
                break;
        }
    }

    /**
     * done entering the pain log information so save it to the local storage
     * and sync immediately with the server so the doctor can view the logs
     */
    @OnClick(R.id.pain_log_done_button)
    public void savePainLog() {

        // force patient to answer both questions
        if (mLog.getSeverity() == PainLog.Severity.NOT_DEFINED
                || mLog.getEating() == PainLog.Eating.NOT_DEFINED ) {
            Toast.makeText(
                    getActivity(),
                    "Please answer both questions.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // save Pain Log to the CP
        mPatientId = LoginUtility.getLoginId(getActivity());
        // associate pain log with a checkin
        mLog.setCheckinId(LoginUtility.getCheckInLogId(getActivity()));
        ContentValues cv = PatientCPcvHelper.createValuesObject(mPatientId, mLog);
        Log.d(LOG_TAG, "Saving this Pain Log : " + mLog.toString());
        Uri uri = getActivity().getContentResolver().insert(PainLogEntry.CONTENT_URI, cv);
        long objectId = ContentUris.parseId(uri);
        if (objectId < 0) {
            Log.e(LOG_TAG, "Pain Log Insert Failed.");
        }

        //try to send to server asap
        SymptomManagementSyncAdapter.syncImmediately(getActivity());

        // tell the activity we're done and if check-in put up the med log fragment
        // also tell the med log fragment to use the same checkin id so we can associate the
        // pain log with the med logs
        boolean isCheckIn = ((Callbacks) getActivity()).onPainLogComplete(mLog.getCheckinId());
        Log.d(LOG_TAG, "Status log and Med logs should have this same id: "
                + Long.toString(mLog.getCheckinId()));
        if (!isCheckIn) {
            getActivity().onBackPressed();
        }
    }
}
