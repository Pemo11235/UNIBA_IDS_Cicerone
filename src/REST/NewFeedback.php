<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['username']) && isset($_POST['tour_id']) && 
	isset($_POST['rate']) && isset($_POST['description']) && 
	isset($_POST['created_on'])) {
	
	// acquisizione delle informazioni generali
	$username = $_POST['username'];
	$tour_id = $_POST['tour_id'];
	$rate = $_POST['rate'];
	$description = $_POST['description'];
	$created_on = $_POST['created_on'];

	// salvataggio del feedback
	$is_stored = $db->store_feedback($username, $tour_id, $rate, $description, $created_on);
		
	// se è stato salvato correttamente
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