<?php

// definizione della dipendenza esterna
require_once 'DBHandler.php';

// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);


// se tutti i campi sono stati completati
if (isset ($_POST ['username'])) {

	// definizione dei vari campi da quelli passati in post
	$username = $_POST ['username'];
    
    // definizione della lista di linguaggi dal database
    $language_list = $db->get_languages ();
    
    $spoken_languages = NULL;
    
    // acquisizione dei linguaggi passati prendendo 
    // l'indice dalla lista di linguaggi del database
    foreach ($language_list as $language) {
        
        // acquisizione dell'id della lingua
        $language_id = $language['id'];
        
        // definizione del valore della lingua passata
        $spoken_languages[$language_id] = $_POST[$language_id]; 
    }
	
    // creazione e salvataggio dell'utente nel database		
    $are_stored = $db->update_spoken_languages ($username, $spoken_languages);
	
    // se il salvataggio e la creazione va a buon fine
	if ($are_stored) {

        // definizione dell'errore
        $response ["language"] = $spoken_languages;
        
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