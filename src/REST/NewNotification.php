<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset($_POST['sender_id']) && isset($_POST['receiver_id']) &&
	isset($_POST['reason_id']) && isset($_POST['created_on']) && 
	isset($_POST['event_id']) && isset($_POST['description'])) {
	
	// acquisizione delle informazioni generali
	$sender = $_POST['sender_id'];
	$receiver = $_POST['receiver_id'];
	$reason = $_POST['reason_id'];
	$created_on = $_POST['created_on'];
	$event_id = $_POST['event_id'];
	$description = $_POST['description'];
	
	$is_stored = $db->store_notification($sender, $receiver, $reason, $created_on, $event_id, $description);
		
	if ($is_stored) {
		
		$response ["message"] = "Everything is ok!";
		
		// invio della risposta sotto forma di json
        echo json_encode ($response);
		
	} else {
		
		// definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in saving the notification.";

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