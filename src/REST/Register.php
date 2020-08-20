<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset ($_POST ['username']) && isset ($_POST ['email']) && isset ($_POST ['password']) && 
	isset ($_POST ['first_name']) && isset ($_POST ['last_name']) && 
	isset ($_POST ['phone_number']) && isset ($_POST ['image_path']) && isset ($_POST ['role'])   ) {

	// definizione dei vari campi da quelli passati in post
	$username = $_POST ['username'];
	$email = $_POST ['email'];
	$password = $_POST ['password'];
	$first_name = $_POST ['first_name'];
	$last_name = $_POST ['last_name'];
	$phone_number = $_POST ['phone_number'];
    $image_path = $_POST ['image_path'];
	$role = $_POST ['role'];

	// se l'username esiste gia
	if ($db->is_username_existing ($username)) {

		// definizione dell'errore
		$response ["error"] = true;

		// definizione del messaggio di errore
		$response ["error_msg"] = "Username already taken";

		// invio della risposta sotto forma di json
		echo json_encode ($response);
	
	} else if ($db->is_email_existing ($email)) { // se la e-mail esiste gia

		// definizione dell'errore
		$response ["error"] = true;
		
		// definizione del messaggio di errore
		$response ["error_msg"] = "E-mail already taken";

		// invio della risposta sotto fora di json
		echo json_encode ($response);	

	} else {

		// creazione e salvataggio dell'utente nel database		
		$user = $db->store_user ($username, $email, $password, $first_name, $last_name, $phone_number, $image_path, $role);
	
		// se il salvataggio e la creazione va a buon fine
		if ($user) {

			// non definizione del errore
			$response ["error"] = FALSE;

			// definizione dei campi dell'utente 
			$response ["user"] ["username"] = $user ["username"];
			$response ["user"] ["email"] = $user ["email"];
			$response ["user"] ["password"] = $user ["password"];
			$response ["user"] ["first_name"] = $user ["first_name"];
			$response ["user"] ["last_name"] = $user ["last_name"];
            $response ["user"] ["image_path"] = $user ["image_path"];
			$response ["user"] ["phone_number"] = $user ["phone_number"];
			$response ["user"] ["role"] = $user ["role"];

			// invio della risposta sotto forma di json
			echo json_encode ($response);
		
		} else {
			
			// definizione dell'errore
			$response ["error"] = TRUE;

			// definizione del messaggio di errore
			$response ["error_msg"] = "Error in signing new user";

			// invio della risposta sotto forma di json
			echo json_encode ($response);
		}		

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