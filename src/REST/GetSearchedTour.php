<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => TRUE);

// se tutti i campi sono stati completati
if (isset($_POST['keywords']) && isset($_POST['username'])) {

	// definizione dei vari campi da quelli passati in post
	$keywords = $_POST ['keywords'];
	$username = $_POST ['username'];

	// acquisizione delle attività sulla base delle keyword inserite
	$tour_list = $db->get_searched_tours($keywords, $username);

	// se si acquisiscono delle attività
	if ($tour_list != null) {

		// definizione dell'errore
		$response ["error"] = FALSE;

		// definizione delle attività
		$response ["tours"] = $tour_list;
		
	} else {
			
		$response["error"] = TRUE;
		
		$response["error_msg"] = "No tour find on database";
	}
	
	echo json_encode($response);


} else { // se qualche campo è incompleto

	// definizione dell'errore
	$response ["error"] = TRUE;

	// definizione del messaggio di errore
	$response ["error_msg"] = "Missing some fields";

	// invio della risposta sotto forma di json
	echo json_encode ($response);
}



?>