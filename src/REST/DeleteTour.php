<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['tour_id']) && isset($_POST['cicerone_id']) &&
	isset($_POST['created_on']) && isset($_POST['description']) &&
	isset($_POST['reason_id'])) {
	
	// acquisizione delle informazioni generali
	$id = $_POST['tour_id'];
	$cicerone_id = $_POST['cicerone_id'];
	$created_on = $_POST['created_on'];
	$description = $_POST['description'];
	$reason_id = $_POST['reason_id'];
	
	// acquisizione degli eventi del tour
	$tour_events = $db->get_events($id);

	// acquisizione degli iscritti a tutti gli eventi del tour
	$subscribeds = $db->get_tour_subscribeds($id);
		
	// per ogni evento acquisito
	foreach ($tour_events as $event) {
		
		// cancellazione del tour
		$is_deleted = $db->delete_event($event["id"]);
		
		// se c'è un errore nella cancellazione
		if(!$is_deleted) {
		
			// definizione dell'errore
			$response ["error"] = TRUE;
			
			// definizione del messaggio di errore
			$response ["error_msg"] = "Error in deleting the tour and its information.";

			// invio della risposta sotto forma di json
			echo json_encode ($response);
		}
	}
	
	// cancellazione del tour
	$is_deleted = $db->delete_tour($id);
	
	// definizione delle notifiche di cancellazione delle sottoscrizioni
	foreach($subscribeds as $sub) {
		
		$db->store_notification($cicerone_id, $sub["username"], $reason_id, $created_on, $sub["event_id"], $description);
	}
	
	// se la cancellazione va a buon fine	
	if ($is_deleted) {
		
		$response ["message"] = "Everything is ok!";
		
		// invio della risposta sotto forma di json
        echo json_encode ($response);
		
	} else {
		
		// definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in deleting the tour and its information.";

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