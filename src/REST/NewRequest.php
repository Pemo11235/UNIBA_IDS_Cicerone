<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['globetrotter']) && isset($_POST['event_id']) &&
	isset($_POST['group_dimension']) && isset($_POST['created_on'])) {
	
	// acquisizione delle informazioni generali
	$globetrotter = $_POST['globetrotter'];
	$event_id = $_POST['event_id'];
	$group_dimension = $_POST['group_dimension'];
	$created_on = $_POST['created_on'];
	
	$is_stored = $db->store_request($globetrotter, $event_id, $group_dimension, $created_on);
		
	if ($is_stored) {
		
		$response ["message"] = "Everything is ok!";
		
		// invio della risposta sotto forma di json
        echo json_encode ($response);
		
	} else {
		
		// definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in saving the request. Probably there is already one for that event.";

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