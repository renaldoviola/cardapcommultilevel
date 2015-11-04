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
import android.widget.Toast;

import com.renaldoviola.cardapcommultilevel.helper.AlertDialogManager;
import com.renaldoviola.cardapcommultilevel.helper.ConnectionDetector;
import com.renaldoviola.cardapcommultilevel.helper.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by renaldo on 30/10/15.
 */
public class MenusListActivity extends ListActivity {

    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> menuList;

    // tracks JSONArray
    JSONObject establishments = null;
    JSONArray menus = null;


    // establishment id
    String establishment_id, establishment_name;

    // tracks JSON url
    // id - should be posted as GET params to get menu list (ex: id = 5)
    private static final String URL_MENUS = "http://cardapcom-rails.herokuapp.com/api/v1/establishments";

    // JSON menus
    private static final String TAG_ID_MEN      = "id";
    private static final String TAG_DATA        = "data";
    private static final String TAG_WEEKDAY     = "weekday";
    private static final String TAG_PRICE       = "price";
    private static final String TAG_STATUS_MEN  = "status";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menus);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(MenusListActivity.this, "Sem conexão com internet",
                    "Por favor, conecte-se com a internet para visualizar os cardápios", false);
            // stop executing code by return
            return;
        }

        // Get Establishment id
        Intent i = getIntent();
        establishment_id = i.getStringExtra("establishment_id");

        // Hashmap for ListView
        menuList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadTracks().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing establishment id, song id
         * */
        /*
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), SingleTrackActivity.class);

                // to get song information
                // both establishment id and song is needed
                String establishment_id = ((TextView) view.findViewById(R.id.establishment_id)).getText().toString();
                String song_id = ((TextView) view.findViewById(R.id.song_id)).getText().toString();

                Toast.makeText(getApplicationContext(), "establishment Id: " + establishment_id + ", Song Id: " + song_id, Toast.LENGTH_SHORT).show();

                i.putExtra("establishment_id", establishment_id);
                i.putExtra("song_id", song_id);

                startActivity(i);
            }
        }); */

    }

    /**
     * Background Async Task to Load all tracks under one establishment
     * */
    class LoadTracks extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MenusListActivity.this);
            pDialog.setMessage("Carregando cardápios...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting tracks json and parsing
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post establishment id as GET parameter
            params.add(new BasicNameValuePair(TAG_ID_MEN, establishment_id));

            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_MENUS, "GET",
                    params);


            // Check your log cat for JSON reponse
            Log.d("Menu List JSON: ", json);

            try {
                JSONObject jsonObj = new JSONObject(json);
                establishments = jsonObj.getJSONObject(
                        "establishments");

                if (establishments != null) {

                    for (int i = 0; i < establishments.length(); i++){

                        menus = jsonObj.getJSONArray("menus");

                        if (menus != null) {
                            // looping through All songs
                            for (int j = 0; j < menus.length(); j++) {
                                JSONObject JOMenu = menus.getJSONObject(j);

                                String id_men = JOMenu.optString(TAG_ID_MEN);
                                String data = JOMenu.optString(TAG_DATA);
                                String weekday = JOMenu.optString(TAG_WEEKDAY);
                                String price = JOMenu.optString(TAG_PRICE);
                                String status_men = JOMenu.optString(TAG_STATUS_MEN);

                                HashMap<String, String> menu = new HashMap<String, String>();

                                menu.put(TAG_ID_MEN, id_men);
                                menu.put(TAG_DATA, data);
                                menu.put(TAG_WEEKDAY, weekday);
                                menu.put(TAG_PRICE, price);
                                menu.put(TAG_STATUS_MEN, status_men);

                                menuList.add(menu);
                            }
                        } else {
                            Log.d("Menus: ", "null");
                        }
                    }
                }else {
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
            // dismiss the dialog after getting all tracks
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            MenusListActivity.this, menuList,
                            R.layout.list_menus, new String[] { "establishment_id", TAG_DATA,
                            TAG_WEEKDAY, TAG_PRICE}, new int[] {
                            R.id.establishment_id, R.id.establishment_name, R.id.menu_id, R.id.menu_data, R.id.menu_diasemana, R.id.menu_preco});
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Establishment name
                    setTitle(establishment_name);
                }
            });

        }

    }

}
