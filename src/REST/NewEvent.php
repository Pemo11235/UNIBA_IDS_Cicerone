<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['tour_id']) && isset($_POST['description']) && 
	isset($_POST['max_subs']) && isset($_POST['start_date']) && 
	isset($_POST['end_date']) && isset($_POST['languages'])) {
	
	// acquisizione delle informazioni generali
	$tour_id = $_POST['tour_id'];
	$description = $_POST['description'];
	$max_subscription = $_POST['max_subs'];
	$start_date = $_POST['start_date'];
	$end_date = $_POST['end_date'];
	$raw_language_array = $_POST['languages'];
	
	$languages = array();
	$splitted_value = explode(',', $raw_language_array);
	
	foreach($splitted_value as $value){
		
		array_push($languages, $value);
	}
		
	
	// acquisizione dell'id dell'evento salvato
	$event_id_stored = $db->store_event($tour_id, $description, $max_subscription, $start_date, $end_date);
		
	// se l'id non è null ed è maggiore di 0 allora si è salvato qualcosa
	if ($event_id_stored > 0) {
	
		// definizione delle notifiche di cancellazione delle sottoscrizioni
		foreach($languages as $language) 
			$db->store_event_spoken_language($event_id_stored, $language);
		
		
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