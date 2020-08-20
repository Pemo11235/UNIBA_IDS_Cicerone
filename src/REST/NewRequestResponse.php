<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['globetrotter']) && isset($_POST['event_id']) &&
	isset($_POST['group_dimension']) && isset($_POST['is_accepted']) &&
	isset($_POST['cicerone']) && isset($_POST['created_on'])) {
	
	// acquisizione delle informazioni generali
	$globetrotter = $_POST['globetrotter'];
	$event_id = $_POST['event_id'];
	$group_dimension = $_POST['group_dimension'];
	$is_accepted = $_POST['is_accepted'];
	$cicerone = $_POST['cicerone'];
	$created_on = $_POST['created_on'];
	
	// conversione della risposta in una sottoscrizione o in nulla
	$is_stored = $db->store_request_response($globetrotter, $event_id, $group_dimension, $is_accepted);
		
	if ($is_stored) {
		
		// in base al fatto se la riciesta è stata accettata, 
		// la ragione di creazione della notifica cambia
		if($is_accepted == "TRUE")
			$reason = "3";
		else
			$reason = "11";
		
		// salvataggio della notifica
		$db->store_notification($cicerone, $globetrotter, $reason, $created_on, $event_id, "NULL");
		
		$response ["message"] = "Everything is ok!";
		
		// invio della risposta sotto forma di json
        echo json_encode ($response);
		
	} else {
		
		// definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in saving the response.";

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