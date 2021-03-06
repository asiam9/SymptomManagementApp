package com.skywomantech.app.symptommanagement.physician;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.skywomantech.app.symptommanagement.R;
import com.skywomantech.app.symptommanagement.data.HistoryLog;
import com.skywomantech.app.symptommanagement.data.Patient;
import com.skywomantech.app.symptommanagement.data.PatientDataManager;

/**
 * This fragment expects a patient to be obtained from the hosting activity.
 * <p/>
 * This fragment displays a list of the combined log records for the patient
 * <p/>
 * This list is used by both the patient and the physician apps.
 * <p/>
 * Clicking on a list item does nothing at the time being. This is a view only list.
 * <p/>
 * Future Enhancement is that it could combine the physician and patient logs.
 * Future Enhancement is that there could be other types of logs.
 * Future Enhancement is the ability to filter logs for display purposes.
 */
public class HistoryLogFragment extends ListFragment {

    private static final String LOG_TAG = HistoryLogFragment.class.getSimpleName();
    public final static String FRAGMENT_TAG = "fragment_history_log";
    public final static String BACKUP_KEY = "allow_back";

    // Notifies the activity about the following events
    // getPatientForHistory - return the current patient to work with
    public interface Callbacks {
        public Patient getPatientForHistory();
    }

    private static Patient mPatient;
    private static boolean allowBackup = false;
    private static HistoryLog[] logList;

    public HistoryLogFragment() {
    }

    /**
     * History log is used by both doctor and patient apps
     * but they have different screen flows so have to allow them to change
     * the home up capability programatically
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Boolean found = getArguments().getBoolean(BACKUP_KEY);
            if (found) allowBackup = found;
        } else allowBackup = false;
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(allowBackup);
        this.setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getString(R.string.empty_list_text));
        this.setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException(activity.getString(R.string.callbacks_message));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPatient = ((Callbacks) getActivity()).getPatientForHistory();
        if (mPatient != null ) Log.d(LOG_TAG, "History Patient is " +mPatient.toString());
        displayLogList(mPatient);
    }

    /**
     * Called by hosting activity to set the patient and redisplay the list
     *
     * @param patient object holding the logs to be displayed
     */
    public void updatePatient(Patient patient) {
        if (patient == null) {
            Log.e(LOG_TAG, "Trying to set history log patient to null.");
            return;
        }
        Log.d(LOG_TAG, "New Patient has arrived!" + patient.toString());
        mPatient = patient;
        displayLogList(mPatient);
    }

    private void displayLogList(Patient patient) {
        if (patient != null) {
            logList = PatientManager.createLogList(mPatient);
            if (logList != null)
                try {
                    setListAdapter(new HistoryLogAdapter(getActivity(), logList));
                } catch (Exception e) {
                    // has something to do with sending patient data to this fragment when it
                    // is in the backstack? this doesn't fix it.. out of time for now
                    Log.e(LOG_TAG, "This gets a null pointer on rotation sometimes. sigh!");
                    logList = new HistoryLog[0];
                    setListAdapter(new HistoryLogAdapter(getActivity(), logList));
                }
        }
    }
}
