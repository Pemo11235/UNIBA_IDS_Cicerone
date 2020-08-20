<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['tour_id']) && isset($_POST['locations'])) {
	
	// acquisizione delle informazioni generali
	$tour_id = $_POST['tour_id'];
	$raw_location_array = $_POST['locations'];
	
	// suddivisione della string in tappe
	$locations = array();
	$splitted_value = explode(',', $raw_location_array);
	foreach($splitted_value as $value){
		
		array_push($locations, $value);
	}
		
	// cancellazione delle tappe associate al tour
	$db->delete_locations($tour_id);	
		
	// definizione delle tappe del tour
	foreach($locations as $location) {
		
		// acquisizione della latitudine 
		$splitted_location = explode('/', $location);
		$latitude = $splitted_location[0];
		$longitude = $splitted_location[1];
		
		// salvataggio del
		$is_stored = $db->store_location($tour_id, $latitude, $longitude);
		
		// se c'è un errore di salvataggio
		if (!$is_stored) {
		
			// definizione dell'errore
			$response ["error"] = TRUE;
			
			// definizione del messaggio di errore
			$response ["error_msg"] = "Error in saving the locations";

			// invio della risposta sotto forma di json
			echo json_encode ($response);
		}			
	}
	
	// se l'id non è null ed è maggiore di 0 allora si è salvato qualcosa
	if ($is_stored) {
	
		$response ["message"] = "Everything is ok!";
		
		// invio della risposta sotto forma di json
        echo json_encode ($response);
		
	} else {
		
		// definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in saving the tour";

        // invio della risposta sotto forma di json
        echo json_encode ($response);
	}	
		
} else { // se non tutti i campi sono stati completati

	//definizione dell'errore
	$response ["error"] = TRUE;	

	// definizione del messaggio di errore
	$response ["error_msg"] = "Missing sone fields";

	// invio della risposta sotto forma di json
	echo json_encode ($response);
}




?>