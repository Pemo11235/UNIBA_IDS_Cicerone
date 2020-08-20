<?php

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// definizione della dipendenza esterna
require_once 'DBHandler.php';
require_once 'PHPMailer/PHPMailer.php';
require_once 'PHPMailer/OAuth.php';
require_once 'PHPMailer/POP3.php';
require_once 'PHPMailer/SMTP.php';
require_once 'PHPMailer/Exception.php';


// definizione di un nuovo gestore della connessione al db
$db = new DBHandler ();

// definizione dell'array associativo di risposta
$response = array("error" => FALSE);

// se tutti i campi sono stati completati
if (isset ($_POST ['email'])) {

	// definizione dei vari campi da quelli passati in post
	$email = $_POST ['email'];
    $object = $_POST ['email_object'];
    $body = $_POST ['email_body'];
    $prepassword = $_POST ['email_password'];
    $conclusion = $_POST ['email_conclusion'];

	if ($db->is_email_existing ($email)) { // se la e-mail esiste gia

        $mail = new PHPMailer(true);

        try {
            
            $password = $db->change_password($email);
        
            //$email = "andrideri96@gmail.com";
            
            $mail->isSMTP();
            $mail->SMTPDebug = 0;
            $mail->Debugoutput = 'html';
            $mail->Host = "smtp.gmail.com";
            $mail->Port = 587;
            $mail->SMTPSecure = 'tls';
            $mail->SMTPAuth = true;
            $mail->Username = "winotechgroup@gmail.com";
            $mail->Password = "Banana33";
            $mail->setFrom('winotechgroup@gmail.com', 'Cicerone');
            $mail->addAddress($email, 'To');

            $mail->Subject = $object;
            $mail->Body    = $body . "\n" . $prepassword . 
                " " . $password . "\n" . $conclusion;   

            $mail->send();

            // definizione dell'errore
            $response ["error"] = false;
        
            // invio della risposta sotto fora di json
            echo json_encode ($response);	
        
        } catch (Exception $e) {
            
            // definizione dell'errore
                $response ["error"] = true;

                // definizione del messaggio di errore
                $response ["error_msg"] = "Error sending mail: {$mail->ErrorInfo}";
        }
        
	} else {

        // definizione dell'errore
		$response ["error"] = true;

		// definizione del messaggio di errore
		$response ["error_msg"] = "Email not found";

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