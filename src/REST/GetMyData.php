<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => TRUE);

// se tutti i campi sono stati completati
if (isset ($_POST ['username'])) {

	// definizione dei vari campi da quelli passati in post
	$username = $_POST ['username'];

	$response ['language'] = NULL;
	$response ['user_spoken_language'] = NULL;
	

	// acquisizione delle lingue 
	$languages = $db->get_languages ();

	// se le lingue acquisite esistono
	if ($languages != null) {

		// definizione dell'errore
		$response ['error'] = FALSE;

		// definizione delle lingue
		$response ['language'] = $languages;
	}

	// acquisizione delle lingue parlate dall'utente tramite le sue credenziali
	$user_spoken_languages = $db->get_user_spoken_languages ($username);

	// se le lingue acquisite esistono
	if ($user_spoken_languages != null) {

		// definizione dell'errore
		$response ['error'] = FALSE;

		// definizione delle lingua parlate dall'utente
		$response ['user_spoken_language'] = $user_spoken_languages;
	}

	echo json_encode($response);


} else { // se qualche campo è incompleto

	// definizione dell'errore
	$response ["error"] = TRUE;

	// definizione del messaggio di errore
	$response ["error_msg"] = "Missing username or password";

	// invio della risposta sotto forma di json
	echo json_encode ($response);
}



?>