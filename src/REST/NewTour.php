<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['name']) && isset($_POST['description']) && isset($_POST['cost']) &&
	isset($_POST['start_date']) && isset($_POST['end_date'])) {
	
	// acquisizione delle informazioni generali
	$name = $_POST['name'];
	$description = $_POST['description'];
	$cicerone = $_POST['cicerone'];
	$cost = $_POST['cost'];
	$start_date = $_POST['start_date'];
	$end_date = $_POST['end_date'];
	
	// inizializzazione dell'array multidimensionale delle tappe
	$locations = array();
	
	// acquisizione delle tappe
	foreach ($_POST as $key=>$value) {
	
		// se in $_POST si trova una tappa (denotata con una sottostringa loc-)
		if(strpos($key, "loc-") !== false){
			
			// si suddivide la stringa in latitudine e longitudine e la si inserisce nell'array
			$splitted_value = explode(',', $value);
			$latitude = $splitted_value[0];
			$longitude = $splitted_value[1];		
			array_push($locations, array($latitude, $longitude));
		}
	}
	
	$is_stored = $db->store_tour($name, $description, $cicerone, $cost, $start_date, $end_date, $locations);
		
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