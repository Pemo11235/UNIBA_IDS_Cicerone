<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['tour_id']) && isset($_POST['name']) &&
	isset($_POST['description']) && isset($_POST['cost']) && 
	isset($_POST['start_date']) && isset($_POST['end_date'])) {
	
	// acquisizione delle informazioni generali
	$id = $_POST['tour_id'];
	$name = $_POST['name'];
	$description = $_POST['description'];
	$cost = $_POST['cost'];
	$start_date = $_POST['start_date'];
	$end_date = $_POST['end_date'];
	
	$is_updated = $db->update_tour($id, $name, $description, $cost, $start_date, $end_date);
		
	if ($is_updated) {
		
		$response ["message"] = "Everything is ok!";
		
		// invio della risposta sotto forma di json
        echo json_encode ($response);
		
	} else {
		
		// definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in updating the tour.";

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