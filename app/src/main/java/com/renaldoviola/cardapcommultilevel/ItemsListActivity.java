package com.renaldoviola.cardapcommultilevel;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
 * Created by renaldo on 10/11/15.
 */
public class ItemsListActivity extends ListActivity {

    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> menuList;
    ArrayList<HashMap<String, String>> itemList;

    // tracks JSONArray
    JSONObject establishments = null;
    JSONArray menus = null;
    JSONArray items = null;


    // establishment id
    String establishment_id, establishment_name, menu_id;

    // tracks JSON url
    // id - should be posted as GET params to get menu list (ex: id = 5)

    // Get Establishment id
    String URL_MENUS = "http://cardapcom-rails.herokuapp.com/api/v1/establishments";

    // OBJECT establishment
    private static final String TAG_EST_NAME = "name";

    String nameEstablishment;

    // JSON menus
    private static final String TAG_ID_MEN      = "id";
    private static final String TAG_WEEKDAY     = "weekday";
    private static final String TAG_PRICE       = "price";
    private static final String TAG_STATUS_MEN  = "status";

    // JSON items
    private static final String TAG_ID_ITEM = "id";
    private static final String TAG_ID_ITEM_MENU = "menu_id";
    private static final String TAG_PLATE_ITEM = "plate";
    private static final String TAG_STATUS_ITEM = "status";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(ItemsListActivity.this, "Sem conexão com internet",
                    "Por favor, conecte-se com a internet para visualizar os cardápios", false);
            // stop executing code by return
            return;
        }

        Intent intent = getIntent();
        establishment_id = intent.getStringExtra("establishment_id");
        menu_id = intent.getStringExtra("menu_id");

        URL_MENUS = URL_MENUS + "/"+ establishment_id;

        // Hashmap for ListView
        itemList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadItems().execute();
    }

    class LoadItems extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ItemsListActivity.this);
            pDialog.setMessage("Carregando refeições ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting song json and parsing
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id, song id as GET parameters
            params.add(new BasicNameValuePair("establishment", establishment_id));
            params.add(new BasicNameValuePair("menu_id", menu_id));

            String json = jsonParser.makeHttpRequest(URL_MENUS, "GET", params);

            Log.d("URL_MENUS: ", URL_MENUS);

            // Check your log cat for JSON reponse
            Log.d("Menu List JSON: ", json);

            try {
                JSONObject jsonObj = new JSONObject(json);

                JSONObject establishment = jsonObj.getJSONObject("establishment");

                nameEstablishment = establishment.optString(TAG_EST_NAME);

                menus = establishment.getJSONArray("menus");


                if (menus != null) {
                    for (int j = 0; j < menus.length(); j++){

                        JSONObject JOMenu = menus.optJSONObject(j);

                        String weekday = JOMenu.optString(TAG_WEEKDAY);
                        String price = JOMenu.optString(TAG_PRICE);
                        String status_men = JOMenu.optString(TAG_STATUS_MEN);

                        items =  JOMenu.getJSONArray("items");

                        if (items != null) {

                            for (int it = 0; it < items.length(); it++) {
                                JSONObject JOItem = items.getJSONObject(it);

                                    String id_item = JOItem.optString(TAG_ID_ITEM);
                                    String plate_item = JOItem.optString(TAG_PLATE_ITEM);
                                    String status_item = JOItem.optString(TAG_STATUS_ITEM);

                                    HashMap<String, String> item = new HashMap<String, String>();

                                    item.put(TAG_ID_ITEM, id_item);
                                    item.put(TAG_PLATE_ITEM, plate_item);
                                    item.put(TAG_STATUS_ITEM, status_item);

                                    itemList.add(item);
                            }
                        }
                    }
                }else {
                        Log.d("Menus: ", "null");
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
            // dismiss the dialog after getting song information
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            ItemsListActivity.this, itemList,
                            R.layout.list_items, new String[] {TAG_ID_ITEM, TAG_PLATE_ITEM},
                            new int[] {R.id.item_id, R.id.plate,});
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Establishment name
                    //setTitle(establishment_name);
                }
            });

        }

    }
}
