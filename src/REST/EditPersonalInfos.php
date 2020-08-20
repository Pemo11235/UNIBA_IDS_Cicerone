<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);


// se tutti i campi sono stati completati
if (isset ($_POST ['username']) && isset ($_POST ['email']) && 
	isset ($_POST ['first_name']) && isset ($_POST ['last_name']) && 
	isset ($_POST ['phone_number']) && isset ($_POST ['biography']) && 
    isset ($_POST ['encoded_image']) && isset ($_POST ['image_dir_url']) &&
    isset ($_POST ['previous_email'])) {

	// definizione dei vari campi da quelli passati in post
	$username = $_POST ['username'];
	$email = $_POST ['email'];
    $previous_email = $_POST ['previous_email'];
	$first_name = $_POST ['first_name'];
	$last_name = $_POST ['last_name'];
	$phone_number = $_POST ['phone_number'];
    $biography = $_POST ['biography'];
    $encoded_image = $_POST ['encoded_image'];
    $image_dir_url = $_POST ['image_dir_url'];
	
    if($previous_email != $email && $db->is_email_existing($email)) {
                
        // non definizione del errore
		$response ["error"] = TRUE;
        
        // definizione del messaggio di errore
        $response ["error_msg"] = "E-mail already taken";

        // invio della risposta sotto forma di json
		echo json_encode ($response);
    
    } else {
        
        // creazione e salvataggio dell'utente nel database		
        $user = $db->update_user_infos ($username, null, $email, $first_name, $last_name, $phone_number, $biography);
	
        // se il salvataggio e la creazione va a buon fine
	    if ($user) {

            // non definizione del errore
            $response ["error"] = FALSE;

            // definizione dell'url dove salvare le immagini
            $url = $image_dir_url;
        
            // definizione del nome dell'immagine
            $image_name = $username . ".jpg";

            // definizione del percorso dove salvare l'immagine
            $image_path = $url . $image_name; // path of saved image 

            // decodifica da base64 a bitmap
            $binary = base64_decode($encoded_image);

            // definizione del tipo di file (per definire il formato dell'immagine)
            header("Content-Type: bitmap; charset=utf-8");

            // apertura del file
            $file = fopen("images/" . $image_name, "wb"); // 
            //$file = fopen($image_path, "wb");
        
            $filepath = $image_name; 
        
            // creazione del file
            fwrite($file, $binary);

            // chiusura del file
            fclose($file);
        
            // definizione dei campi dell'utente 
            $response ["user"] ["username"] = $user ["username"];
            $response ["user"] ["email"] = $user ["email"];
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