package com.ybs.myroute;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, PermissionsListener, LocationEngineListener {
    private MapView mapView;
    private MapboxMap map;
    private Button goBtn, backBtn;
    private EditText edtTarget, edtMyLocation;
    private double lat, lng;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private Point originPosition, destinationPosition;
    private int id=0, flagTimeBtn = 0, id1 = 1, i, j;
    private Marker destinationMarker;
    private NavigationMapRoute navigationMapRoute;
    private static final String TAG = "MainActivity";
    private LatLng location, destinationPoint;
    private List<LatLng> listLocation = new ArrayList<>();
    private List <EditText> editTexts = new ArrayList<EditText>();
    private ImageView imgvPluse;
    private String geojsonSourceLayerId = "geojsonSourceLayerId", msg = " ",myLocation = "";
    private String symbolIconId = "symbolIconId", placelocation = " ";
    private LinearLayout linearLayoutlist, addEdt;
    private ArrayList<String> places;
    ArrayAdapter arrayAdapter;
    private ListView list;
    private NotificationManager notificationManager;
    private static String CHANNEL1_ID = "channel1";
    private static String CHANNEL1_NAME = "Channel 1 Demo";
    private BatteryReceiver batteryReceiver;
    private IntentFilter intentFilter;
    private double [][] matrixDis;
    private Point [] points;
    private LatLng [] latLngs;
    private List<MarkerOptions> listMarker = new ArrayList<MarkerOptions>();
    private List <DirectionsRoute> listRoute= new ArrayList<DirectionsRoute>();
    private MarkerOptions markerOptions;
    int indexI=-1,indexJ=-1;
    double distance=0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiYmluYXJvIiwiYSI6ImNqeGJrbmg5NjA1OGwzenBlZjV3emdyajkifQ.tnGwsVpv3yV2HIqGei_Qiw");
        setContentView(R.layout.activity_main);
        goBtn = findViewById(R.id.btnGo);
        goBtn.setOnClickListener(this);
        backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(this);
        edtTarget = findViewById(R.id.edtTarget);
        edtMyLocation = findViewById(R.id.edtStart);
        edtMyLocation.setOnClickListener(this);
        imgvPluse = findViewById(R.id.imgvPlus);
        imgvPluse.setOnClickListener(this);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        SharedPreferences sp = getSharedPreferences("file", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        linearLayoutlist = findViewById(R.id.list);
        list = findViewById(R.id.listID);
        places = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);
        list.setAdapter(arrayAdapter);
        //This is items of your places
        LatLng Home = new LatLng(31.863157, 35.160566);
        placelocation = convertLocation(Home.getLatitude(), Home.getLongitude());
        editor.putString("Home", placelocation);
        LatLng Work = new LatLng(31.804030, 35.213449);
        placelocation = convertLocation(Work.getLatitude(), Work.getLongitude());
        editor.putString("Work", placelocation);
        LatLng SuperMarket = new LatLng(31.788360, 35.185019);
        placelocation = convertLocation(SuperMarket.getLatitude(), SuperMarket.getLongitude());
        editor.putString("SuperMarket", placelocation);
        editor.commit();

        //Notification
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Create channel only if it is not already created
            if (notificationManager.getNotificationChannel(CHANNEL1_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL1_ID,
                        CHANNEL1_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        // Create the Broadcast Receiver
        batteryReceiver = new BatteryReceiver();
        // Define the IntentFilter.
        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // Register the receiver & filter
        registerReceiver(batteryReceiver, intentFilter);
        //List of your places
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Add item to target point
                edtTarget.setText(places.get(position));
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.edtStart: //The defult is your location to change need to click on the start edit text
                edtMyLocation.setText("");
                break;
            case R.id.edtTarget:
                break;
            case R.id.imgvPlus: //Click to add a target point
                flagTimeBtn++;
                addEditText(); //Fanction that add an edit text
                break;
            case R.id.btnGo: //Click to start navigation
                Boolean flag = clickBtnGo();//Check if all edit text have a true location
                if(flag == false){ //Shows only the map to rest for the user while traveling
                    checkShortRoute();
                    edtMyLocation.setVisibility(View.GONE);
                    edtTarget.setVisibility(View.GONE);
                    imgvPluse.setVisibility(View.GONE);
                    goBtn.setVisibility(View.GONE);
                    if(flagTimeBtn>0){ //If there is more then 1 target
                        addEdt.setSystemUiVisibility(View.GONE);
                        addEdt.setVisibility(View.GONE);
                    }
                    linearLayoutlist.setSystemUiVisibility(View.GONE);
                    linearLayoutlist.setVisibility(View.GONE);
                    backBtn.setVisibility(View.VISIBLE);
                    backBtn.setEnabled(true);
                }
                else {
                    Toast.makeText(MainActivity.this, "One of the location is not right", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnBack: //If you start navigat and you need to change the route or locations
                linearLayoutlist.setVisibility(View.GONE);
                if(flagTimeBtn>0) { //If there is more the 1 target
                    addEdt.removeAllViews();

                }
                navigationMapRoute.removeRoute();
                map.clear();

                map.removeMarker(markerOptions.getMarker());
                markerOptions.getMarker().remove();

                listMarker = new ArrayList<MarkerOptions>();
                listRoute= new ArrayList<DirectionsRoute>();
                indexI=-1;
                indexJ=-1;
                distance=0.0;
                for(i=0;i<listLocation.size();i++){
                    points[i]=null;
                    latLngs[i]= null;
                }
                listLocation.clear();
                listLocation= new ArrayList<>();


                edtMyLocation.setVisibility(View.VISIBLE);
                edtTarget.setVisibility(View.VISIBLE);
                edtTarget.setText("");
                imgvPluse.setVisibility(View.VISIBLE);
                goBtn.setVisibility(View.VISIBLE);

                backBtn.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        map = mapboxMap;
        enableLocation();

    }
    //Enable my location
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initilizeLocationEngine();
            initilizeLocationLayer();
        } else {
            permissionsManager = new PermissionsManager((this));
            permissionsManager.requestLocationPermissions(this);
        }
    }
    //To find my location with getLastLocation()
    @SuppressLint("MissingPermission")
    public void initilizeLocationEngine() {
        locationEngine = new LocationEngineProvider(this).obtainBestLocationEngineAvailable();
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();
        Location lastLocation = locationEngine.getLastLocation();
        myLocation = convertLocation(lastLocation.getLatitude(), lastLocation.getLongitude());
        edtMyLocation.setText(myLocation); //Get my location in the start edit text
        if(lastLocation!= null){
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        }else {
            locationEngine.addLocationEngineListener(this);
        }
    }
    @SuppressLint("MissingPermission")
    private void initilizeLocationLayer(){
        locationLayerPlugin = new LocationLayerPlugin(mapView, map,locationEngine);
        locationLayerPlugin.setLocationLayerEnabled(true);
        locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
        locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
    }
    private void setCameraPosition(Location location){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 24.0));

    }




    //Check the shortest route of the points
    private void checkShortRoute() {
        matrixDis = new double[listLocation.size()][listLocation.size()];
        for(i=0; i<matrixDis.length; i++) {
            for (j = 0; j < matrixDis.length; j++) {
                matrixDis[i][j]=-1.0;
            }
        }
        points = new Point[listLocation.size()];
        latLngs = new LatLng[listLocation.size()];
        for (i=0; i<listLocation.size(); i++){
            latLngs[i]=listLocation.get(i);
        }
        for(i=0; i<points.length; i++)//Fulling the points array
        {
            points[i]=Point.fromLngLat(latLngs[i].getLatitude(), latLngs[i].getLongitude());
            Log.d("debug", "tr 1 "+points[i].latitude());
        }
        for(i=0; i<points.length; i++)
        {
            for(j=i+1; j<points.length; j++)
            {
                getDistance(points[i], points[j], i, j);

            }
        }

    }
    //Function that calculate the distance betweem every 2 point
    private void getDistance(Point origin, Point destination, int i, int j)
    {

        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>()
                {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response)
                    {
                        double d=0.0;
                        if(response.body() == null)
                        {
                            Log.e(TAG, "No routes found, check right user access token");
                            return;
                        }
                        else if(response.body().routes().size() ==0)
                        {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        DirectionsRoute currentRoute =  response.body().routes().get(0);
                        d = currentRoute.distance()/1000.0;
                        distance=d;
                        indexI=i;
                        indexJ=j;
                        calculateDistance(distance, indexI, indexJ);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t)
                    {
                        Log.e(TAG, "Error:"+t.getMessage());
                    }

                });
    }
    //Function that get te distace of 2 poit and put it in the array
    public  void  calculateDistance(double d, int i, int j)
    {
        if(matrixDis[i][j]==-1.0 && matrixDis[j][i]==-1.0)
        {
            matrixDis[i][j]=d;
            matrixDis[j][i]=d;
        }
        for(int k=0; k<matrixDis.length; k++) {
            matrixDis[k][k] = 0.0;
        }
        cal();
    }

    public void cal() //Prepares the matrix that can send it to the algorithm that calculates the shortest path
    {
        boolean flag=false;
        for(int l=0; l<matrixDis.length;l++) {
            for (int q = 0; q < matrixDis.length; q++) {
                if(matrixDis[l][q]==-1.0)
                {
                    flag=true;
                    break;
                }

            }
            if(flag)
                break;
        }
        if(flag==false)
        {
            double [][] finalMatrix = new double[matrixDis.length+1][matrixDis.length+1];
            finalMatrix[0][0] = matrixDis[0][0];
            for(int l=0; l<matrixDis.length;l++)//Multiply the first row
                finalMatrix[l+1][0] = matrixDis[l][0];

            for (int q =0; q<matrixDis.length; q++)//Multiply the first column
                finalMatrix[0][q+1] = matrixDis[0][q];

            for(int l=0; l<matrixDis.length;l++)//Filling the matrix of distances
                for (int q = 0; q < matrixDis.length; q++)
                    finalMatrix[l+1][q+1]=matrixDis[l][q];


            ShortestPath tspNearestNeighbour = new ShortestPath();
            String path = tspNearestNeighbour.tsp(finalMatrix);// Received string of path like "1 3 2"
            String[] pathPoints = path.split(" ");
            Point [] points1 = new Point[points.length];
            int []indexPoints = new int[points.length];
            for(i=0; i<pathPoints.length; i++)
            {
                indexPoints[i]= Integer.parseInt(pathPoints[i]);//The order of the destination point
            }
            //Arrange the array of points in the order of the shortest route
            for(i=0; i<indexPoints.length; i++) {
                int t = indexPoints[i] - 1;
                points1[t] = points[i];
            }
            //Draw the track and add a marker for each destination
            for(i=0; i<indexPoints.length-1; i++)
            {
                getRoute(points1[i], points1[i+1],i+1);//Functio to draw the route
                destinationPoint = new LatLng( points1[i+1].latitude(),  points1[i+1].longitude());
                markerOptions = new MarkerOptions().position(destinationPoint).title(i+1+"");//Add a marker in the destination point
                listMarker.add(markerOptions);
                map.addMarkers(listMarker);

            }
        }

    }

    //Function get 2 point - lat, lng and return the name of the place
    private String convertLocation(double lat, double lon){
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = null;
        try
        {
            addresses = geocoder.getFromLocation(lat, lon, 1); //Get the name
        }
        catch (Exception e) {e.printStackTrace();}

        if(addresses != null && addresses.size()>0)
        {
            msg = addresses.get(0).getAddressLine(0);
            return msg; //If the 2 point is exist
        }
        return null;
    }
    //The user clicked on the btnGo
    public Boolean clickBtnGo(){
        Boolean flag = false;
        location = checkLocationTrue(edtMyLocation.getText().toString());//Check the start point
        if(location.getLatitude() ==0.0 && location.getLongitude() == 0.0){//Check if the edit text is empty
            flag=true;
        }
        else{
            listLocation.add(location); //Add to list of the location
        }
        location = checkLocationTrue(edtTarget.getText().toString());//Check the first target point
        if(location.getLatitude() ==0.0 && location.getLongitude() == 0.0){ //Check if the edit text is empty
            flag=true;
        }
        else{
            listLocation.add(location); //Add to list of the location
        }
        if(flagTimeBtn>0){
            for(int i=1; i <= flagTimeBtn; i++){
                location = checkLocationTrue(editTexts.get(i-1).getText().toString()); //Check the list of the target points
                if(location.getLatitude() ==0.0 && location.getLongitude() == 0.0){//Check if the edit text is empty
                    flag=true;
                }
                else{
                    listLocation.add(location); //Add to list of the location
                }
            }

        }

        return flag; //True if one of the edit text is empty
    }


//Function get string of location and return the 2 point lat, lng of this location
   private LatLng checkLocationTrue(String toString) {
        LatLng latLngOfLocation = new LatLng(0,0);
        if(toString.equals("")){
            Toast.makeText(MainActivity.this, "Please enter all locations", Toast.LENGTH_SHORT).show();
            return latLngOfLocation;
        }
        String location = toString;
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> addresses = null;
        try
        {
            addresses = geocoder.getFromLocationName(location,1); //Get name and return 2 double point- lat, lng
            lat = addresses.get(0).getLatitude();
            lng = addresses.get(0).getLongitude();
            latLngOfLocation = new LatLng(lng, lat);
        }
        catch (Exception e) {e.printStackTrace();}

        if(addresses != null && addresses.size()>0)
        {
            return latLngOfLocation;
        }
        return latLngOfLocation;
    }

    //Add my places to a list with Shared Preferences
    private void addTaskToList() {
        SharedPreferences sp = getSharedPreferences("file", Context.MODE_PRIVATE);
        linearLayoutlist.setVisibility(View.VISIBLE);
        places.add(sp.getString("Home",null));
        places.add(sp.getString("Work", null));
        places.add(sp.getString("SuperMarket", null));
        arrayAdapter.notifyDataSetChanged();
    }
    //Add the list of the target edit text when click on the + button
    public void addEditText()
    {
        EditText newEdt = new EditText(this);
        id++;
        newEdt.setId(id);
        newEdt.setText("");
        newEdt.setHint("Target point "+ id);
        newEdt.setBackground(edtTarget.getBackground());
        newEdt.setTextSize(20);

        addEdt = findViewById(R.id.targetList);
        addEdt.addView(newEdt);
        editTexts.add(newEdt);
    }
    //Function that draw the route between every 2 points
    private void getRoute(Point origin, Point destination, int index)
    {
        destinationPosition = destination;
        destinationPoint = new LatLng(destinationPosition.latitude(), destinationPosition.longitude());
        NavigationRoute.builder()
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if(response.body() == null)
                        {
                            Log.e(TAG, "No routes found, check right user access token");
                            return;
                        }
                        else if(response.body().routes().size() ==0){
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        DirectionsRoute currentRoute = response.body().routes().get(0);

                        listRoute.add(currentRoute);
                        if(navigationMapRoute != null)
                        {

                            //navigationMapRoute.removeRoute();
                            navigationMapRoute.addRoutes(listRoute);
                            destinationMarker = map.addMarker(new MarkerOptions().position(destinationPoint).title(index+" "));
                        }
                        else
                        {

                            navigationMapRoute = new NavigationMapRoute(null, mapView, map);

                            navigationMapRoute.addRoute(currentRoute);
                            destinationMarker = map.addMarker(new MarkerOptions().position(destinationPoint).title(index+" "));
                        }

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error:"+t.getMessage());
                    }
                });
    }



    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        locationEngine.removeLocationUpdates();
    }

    //chack if my location changed
    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            originLocation = location;
            setCameraPosition(location);
        }

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {


    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            enableLocation();
        }

    }
    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    // optiones of the item in menu clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                AlertDialog.Builder alert_about = new AlertDialog.Builder(this);
                alert_about.setTitle("About Truquick");
                alert_about.setMessage("This app navigates between several destinations \n\nBy Hodaya Siri and Bina Rosental 2019");
                alert_about.create().show();
                break;
            case R.id.exit: //to exit the app
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Exit Truquick");
                alert.setMessage("Do you realy want to Exit?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("Debug", "Yes");
                        finish(); // destroy this activity
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Log.d("Debug", "No");
                    }
                });
                alert.create().show();
                break;
            case R.id.share: //to share my location with SMS massege
                Intent i = new Intent(MainActivity.this,ShareMyLocation.class);
                i.putExtra("myLocation","My location: "+myLocation);
                startActivity(i);
                break;
            case R.id.places: //list of yuor saved places
                addTaskToList();
                break;
        }


        return super.onOptionsItemSelected(item);
    }



    //notification
    public void showNotification()
    {
        String notificationTitle = "Truquick";
        String notificationText = "Running in the background...";
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL1_ID)
                .setSmallIcon(R.drawable.truquick )          //Set the icon
                .setContentTitle(notificationTitle)         //Set the title of Notification
                .setContentText(notificationText)           //Set the text for notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        // Send the notification to the device Status bar.
        notificationManager.notify(id1, notification);


    }


    //life cycle
    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if(locationEngine != null){
            locationEngine.requestLocationUpdates();
        }
        if(locationLayerPlugin != null){
            locationLayerPlugin.onStart();
        }
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        showNotification();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(locationEngine != null){
            locationEngine.removeLocationUpdates();
        }
        if(locationLayerPlugin != null){
            locationLayerPlugin.onStop();
        }
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationEngine != null){
            locationEngine.deactivate();
        }
        // unregister the receiver
        unregisterReceiver(batteryReceiver);
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

    }



}
