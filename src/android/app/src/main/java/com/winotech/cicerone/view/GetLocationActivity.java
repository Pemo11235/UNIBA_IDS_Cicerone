package com.winotech.cicerone.view;

import androidx.fragment.app.FragmentActivity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.winotech.cicerone.R;
import com.winotech.cicerone.controller.TourController;
import com.winotech.cicerone.misc.Constants;
import com.winotech.cicerone.model.Location;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.Vector;

public class GetLocationActivity extends FragmentActivity implements OnMapReadyCallback, Serializable{

    private TourController tourController;

    // mappa di Google Maps
    private GoogleMap map;

    // lista delle tappe segnalate
    private List<Marker> markers;

    // oggetti grafici dell'activity
    private Button resetButton;
    private Button submitButton;


    /*
    Metodo per la creazione dell'activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // inflating dell'activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);


        // inizializzazione del fragment della mappa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // inizializzazione della lista di tappe
        markers = new LinkedList<>();

        tourController = TourController.getInstance();


        // inizializzazione degli oggetti grafici
        resetButton = findViewById(R.id.button6);
        submitButton = findViewById(R.id.button7);

        // settaggio del listener del bottone di reset
        resetButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                map.clear();
                markers.clear();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // definizione di una nuova lista di tappe
                HashMap<String, Location> locations = new HashMap<>();

                for (Marker m : markers){

                    Location location = new Location();
                    String name = null;

                    location.setLatitude((float) m.getPosition().latitude);
                    location.setLongitude((float) m.getPosition().longitude);

                    Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {

                        List<Address> addresses = geo.getFromLocation(m.getPosition().latitude,
                                m.getPosition().longitude, 1);
                        Address obj = addresses.get(0);

                        name = obj.getAddressLine(0);

                    } catch (IOException e) {

                        e.printStackTrace();
                    }

                    locations.put(name, location);
                }

                // definisci un collegamento all'actvity della pagina principale
                Intent intent = new Intent (GetLocationActivity.this, MainActivity.class);

                intent.putExtra(Constants.FRAGMENT_TO_LOAD_KEY, Constants.NEW_TOUR_FRAGMENT_VALUE);
                intent.putExtra("SET_LOCATIONS", locations);

                // avviamento dell'activity
                startActivity (intent);

                // chiusura di questa activity
                finish ();

            }
        });
    }


    /*
    Metodo per l'inizializzazione della mappa e delle sue componenti
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // inizializzazione della mappa
        map = googleMap;

        HashMap<String, Location> alreadySetLocation;
        if(tourController.hasTempLocations()) {

            alreadySetLocation = new HashMap<>(tourController.popTempLocation());

            for (Location loc : alreadySetLocation.values()) {

                // creazione di un marker di posizione
                MarkerOptions marker = new MarkerOptions();

                // settaggio della posizione del marker
                marker.position(new LatLng(loc.getLatitude(), loc.getLongitude()));

                // settaggio del titolo del marker che verrà mostrato a video
                marker.title(loc.getLatitude() + " : " + loc.getLongitude());

                // inserimento del marker alla posizione definita nella mappa
                // e successivo inserimento nella lista
                markers.add(map.addMarker(marker));

                // inserimento del listener del click sul marker
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        markers.remove(marker);

                        // rimozione del marker se viene selezionato
                        marker.remove();
                        return true;
                    }
                });
            }
        }

        // Settaggio del listener del click sulla mappa
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // creazione di un marker di posizione
                MarkerOptions marker = new MarkerOptions();

                // settaggio della posizione del marker
                marker.position(latLng);

                // settaggio del titolo del marker che verrà mostrato a video
                marker.title(latLng.latitude + " : " + latLng.longitude);

                // animazione per lo spostamento della camera sul punto selezionato
                map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // inserimento del marker alla posizione definita nella mappa
                // e successivo inserimento nella lista
                markers.add(map.addMarker(marker));

                // inserimento del listener del click sul marker
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {

                        markers.remove(marker);

                        // rimozione del marker se viene selezionato
                        marker.remove();
                        return true;
                    }
                });

                // messaggio di debug per la corretta esecuzione
                System.out.println("Set marker successfully");
            }
        });




    }


}
