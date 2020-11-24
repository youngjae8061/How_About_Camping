package com.example.how_about_camping;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.how_about_camping.WeatherActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class SearchActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private SearchResultAdapter adapter;
    private ArrayList<AddressData> addressList;
    final String TAG = "location";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        recyclerView = (RecyclerView) findViewById(R.id.search_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        addressList = new ArrayList<>();
        adapter = new SearchResultAdapter(addressList);
        recyclerView.setAdapter(adapter);

        searchView = (SearchView)findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.setFilter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder>{
        ArrayList<AddressData> mlist;

        SearchResultAdapter(ArrayList<AddressData> list){
            mlist = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent,false);

            return new SearchResultAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AddressData item = mlist.get(position);
            double lat = item.getLat();
            double lon = item.getLon();


            holder.address.setText(item.getAddress());
            holder.lat = lat;
            holder.lon = lon;
        }

        @Override
        public int getItemCount() {
            return mlist.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder{
            TextView address;
            double lat;
            double lon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                address =(TextView) itemView.findViewById(R.id.address);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String temp = (String)address.getText();
                        temp = AddressParsingUtil.getSigunguFromVWorldAddress(temp);

                        PreferenceManager.setFloat(getApplicationContext(),"LATITUDE",(float)lat);
                        PreferenceManager.setFloat(getApplicationContext(),"LONGITUDE",(float)lon);
                        PreferenceManager.setBoolean(getApplicationContext(),"IS_ADDRESS_CHANGED",true);
                        PreferenceManager.setString(getApplicationContext(),"CITY",temp);

                        Toast.makeText(getApplicationContext(), "주소가 "+temp+"로 설정되었습니다",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "DAta3: " + lat + lon);
                        finish();
                        startActivity(new Intent(getApplicationContext(),WeatherActivity.class));



                    }
                });
            }
        }

        public void setFilter(String query){

            mlist.clear();
            String url="http://api.vworld.kr/req/search?key=65C8E364-157A-30AB-9C5B-C7C113B74773&service=search&request=search&type=address&query=";
            url += query+"&category=road&size=100";
            Log.e("SEULGI SEARCH API URL", url);

            JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject res = response.getJSONObject("response");
                        Log.e("SEULGI SEARCH STATUS",res.getString("status"));
                        if(res.getString("status").equals("OK")){
                            JSONObject result = res.getJSONObject("result");
                            JSONArray items = result.getJSONArray("items");
                            for(int i=0;i< items.length();i++){
                                String address = items.getJSONObject(i).getJSONObject("address").getString("road");
                                double lat = items.getJSONObject(i).getJSONObject("point").getDouble("y");
                                double lon = items.getJSONObject(i).getJSONObject("point").getDouble("x");
                                mlist.add(new AddressData(address,lat,lon));
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"주소가 잘못되었습니다.",Toast.LENGTH_SHORT).show();
                        }

                        notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("SEULGI SEARCH API ERROR",error.toString());
                }
            });

            RequestQueue queue =  Volley.newRequestQueue(getApplicationContext());
            queue.add(jor);
        }

    }

}