<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset ($_POST ['email']) && isset ($_POST ['password'])) {

	// definizione dei vari campi da quelli passati in post
	$email = $_POST ['email'];
	$password = $_POST ['password'];

	// acquisizione dell'utente tramite le sue credenziali
	$user = $db->get_user ($email, $password);

	// se l'utente acquisito esiste
	if ($user != null) {

		// definizione dell'errore
		$response ['error'] = FALSE;

		// definizione dei campi dell'utente
		$response ["user"] ["username"] = $user ["username"];
		$response ["user"] ["email"] = $user ["email"];
		$response ["user"] ["password"] = $password;
		$response ["user"] ["first_name"] = $user ["first_name"];
		$response ["user"] ["last_name"] = $user ["last_name"];
		$response ["user"] ["phone"] = $user ["phone"];
		$response ["user"] ["image_path"] = $user ["image_path"];
		$response ["user"] ["biography"] = $user ["biography"];
		$response ["user"] ["role"] = $user ["role"];
		
		// invio della risposta sotto forma di json
		echo json_encode ($response);
	
	} else { // se l'utente non è stato trovato
	
		// definizione dell'errore
		$response ["error"] = TRUE;
        
		// definizione del messaggio di errore
		$response ["error_msg"] = "Username or password are wrong!";

		// invio della risposta sotto forma di json
		echo json_encode ($response);
	}

} else { // se qualche campo è incompleto

	// definizione dell'errore
	$response ["error"] = TRUE;

	// definizione del messaggio di errore
	$response ["error_msg"] = "Missing username or password";

	// invio della risposta sotto forma di json
	echo json_encode ($response);
}

?>