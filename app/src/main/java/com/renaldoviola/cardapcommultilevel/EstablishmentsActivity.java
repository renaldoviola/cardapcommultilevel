package com.renaldoviola.cardapcommultilevel;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.renaldoviola.cardapcommultilevel.helper.AlertDialogManager;
import com.renaldoviola.cardapcommultilevel.helper.ConnectionDetector;
import com.renaldoviola.cardapcommultilevel.helper.JSONParser;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EstablishmentsActivity extends ListActivity {


    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> establishmentList;

    // establishments JSONArray
    JSONArray establishments = null;

    // establishments JSON url
    private static final String URL_ESTABLISHMENTS = "http://cardapcom-rails.herokuapp.com/api/v1/establishments";

    // JSON Node establishments
    private static final String TAG_ID           = "id";
    private static final String TAG_NAME         = "name";
    private static final String TAG_NEIGHBORHOOD = "neighborhood";
    private static final String TAG_ZIPCODE      = "zipcode";
    private static final String TAG_CITY         = "city";
    private static final String TAG_STATE        = "state";
    private static final String TAG_STATUS_EST   = "status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establishments);

        cd = new ConnectionDetector(getApplicationContext());

        // Check for internet connection
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(EstablishmentsActivity.this, "Sem conexão com internet",
                    "Por favor, conecte-se com a internet para visualizar os cardápios", false);
            // stop executing code by return
            return;
        }

        // Hashmap for ListView
        establishmentList = new ArrayList<HashMap<String, String>>();

        // Loading Establishments JSON in Background Thread
        new LoadEstablishments().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview item click listener
         * TrackListActivity will be lauched by passing establishment id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // on selecting a single establishment
                // TrackListActivity will be launched to show tracks inside the establishment
                Intent intent = new Intent(getApplicationContext(), MenusListActivity.class);

                // send establishment id to tracklist activity to get list of songs under that establishment
                String establishment_id = ((TextView) view.findViewById(R.id.establishment_id)).getText().toString();
                intent.putExtra("establishment_id", establishment_id);


                startActivity(intent);
            }
        });
    }

    /**
     * Background Async Task to Load all Establishments by making http request
     * */
    class LoadEstablishments extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EstablishmentsActivity.this);
            pDialog.setMessage("Listando estabelecimentos...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Establishments JSON
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // getting JSON string from URL

            String json = jsonParser.makeHttpRequest(URL_ESTABLISHMENTS, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("Establishments JSON: ", "> " + json);

            try {
                JSONObject jsonObj = new JSONObject(json);
                establishments = jsonObj.getJSONArray("establishments");

                if (establishments != null) {
                    // looping through All Establishments
                    for (int i = 0; i < establishments.length(); i++) {
                        JSONObject JOEstablishment = establishments.getJSONObject(i);

                        // Storing each json item values in variable
                        String id = JOEstablishment.optString(TAG_ID);
                        String name = JOEstablishment.optString(TAG_NAME);
                        String city = JOEstablishment.optString(TAG_CITY);
                        String neighborhood = JOEstablishment.optString(TAG_NEIGHBORHOOD);
                        String zipcode = JOEstablishment.optString(TAG_ZIPCODE);
                        String state = JOEstablishment.optString(TAG_STATE);
                        String status_est = JOEstablishment.optString(TAG_STATUS_EST);

                        // tmp hashmap for single contact
                        HashMap<String, String> establishment = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        establishment.put(TAG_ID, id);
                        establishment.put(TAG_NAME, name);
                        establishment.put(TAG_CITY, city);
                        establishment.put(TAG_NEIGHBORHOOD, neighborhood);
                        establishment.put(TAG_ZIPCODE, zipcode);
                        establishment.put(TAG_STATE, state);
                        establishment.put(TAG_STATUS_EST, status_est);

                        // adding contact to contact list
                        establishmentList.add(establishment);
                    }
                }else{
                    Log.d("Establishments: ", "null");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all Establishments
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            EstablishmentsActivity.this, establishmentList,
                            R.layout.list_establishments, new String[] {TAG_ID,
                            TAG_NAME}, new int[] {
                            R.id.establishment_id, R.id.establishment_name});

                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}
