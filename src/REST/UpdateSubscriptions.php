<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['username']) && isset($_POST['is_cicerone'])) {
	
	// acquisizione delle informazioni generali
	$username = $_POST['username'];
	$is_cicerone = $_POST['is_cicerone'];
	
	// aggiornamento della lista di iscrizioni
	$is_stored = $db->update_subscriptions ($username, $is_cicerone);
		
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