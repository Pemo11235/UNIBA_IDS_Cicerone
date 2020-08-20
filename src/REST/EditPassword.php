<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);


// se tutti i campi sono stati completati
if (isset ($_POST ['username']) && isset ($_POST ['password'])) {

	// definizione dei vari campi da quelli passati in post
	$username = $_POST ['username'];
	$password = $_POST ['password'];
	
    // creazione e salvataggio dell'utente nel database		
    $user = $db->update_user_infos ($username, $password, null, null, null, null, null);
	
    // se il salvataggio e la creazione va a buon fine
	if ($user) {

        // definizione dei campi dell'utente 
        $response ["user"] ["username"] = $user ["username"];
        $response ["user"] ["email"] = $user ["email"];
        $response ["user"] ["password"] = $password;
        $response ["user"] ["first_name"] = $user ["first_name"];
        $response ["user"] ["last_name"] = $user ["last_name"];
        $response ["user"] ["image_path"] = $user ["image_path"];
        $response ["user"] ["phone_number"] = $user ["phone"];
        $response ["user"] ["biography"] = $user ["biography"];

        // invio della risposta sotto forma di json
        echo json_encode ($response);
		
    } else {
			
        // definizione dell'errore
        $response ["error"] = TRUE;
		
        // definizione del messaggio di errore
        $response ["error_msg"] = "Error in changing the password";

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