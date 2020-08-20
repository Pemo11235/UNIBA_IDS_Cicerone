<?php

class DBHandler {
	
	// variabile per la connessione al database
	private $connection;


	// Costruttore della classe
	function __construct () {
	
		// apertura della dipendenza esterna per la connessione
		require_once 'Connection.php';

		// creazione di una nuova connessione al database
		$db = new Connection ();

		// definizione della connessione funzionante
		$this->connection = $db->connect ();
	}


	// Distruttore della classe
	function __destruct () {
	
	}

    
	
    // ------ FUNZIONI DI SALVATAGGIO ------

	// Metodo per la creazione di un nuovo utente e il suo salvataggio nel database
	public function store_user ($username, $email, $password, $first_name, $last_name, $phone_number, $image_path, $role) {

		if($role == "1")
			$role = true;
		else
			$role = false;

		// criptazione della password fornita
		$encrypted_password = password_hash($password, PASSWORD_DEFAULT);

		// definizione della query di salvataggio del nuovo utente
		$statement = $this->connection->prepare ("INSERT INTO user (username, email, password, first_name, 
			last_name, image_path, phone, biography) VALUES (?, ?, ?, ?, ?, ?, ?, NULL)");

		// binding dei dati da inserire nella query
		$statement->bind_param ("sssssss", $username, $email, $encrypted_password, $first_name, $last_name, 
			$image_path, $phone_number);

		// esecuzione della query
		$result = $statement->execute ();

		// chiusura dello statement
		$statement->close ();
        
        if($role) {
            
            // definizione della query di salvataggio del nuovo cicerone
            $statement = $this->connection->prepare ("INSERT INTO cicerone (user_id) VALUES (?)");
            
            // binding dei dati da inserire nella query
            $statement->bind_param ("s", $username);
            
            // esecuzione della query
            $result = $statement->execute ();

            // chiusura dello statement
            $statement->close ();
            
        } else {
            
            // definizione della query di salvataggio del nuovo cicerone
            $statement = $this->connection->prepare ("INSERT INTO globetrotter (user_id) VALUES (?)");
            
    		// binding dei dati da inserire nella query
            $statement->bind_param ("s", $username);
            
            // esecuzione della query
            $result = $statement->execute ();

            // chiusura dello statement
            $statement->close ();
        }

		// se la query va abuon fine
		if ($result) {
			
			// definizione della query di acquisizione dei dati inseriti
			$statement = $this->connection->prepare ("SELECT * FROM user WHERE username = ?");

			// binding dei dati da inserire nella query
			$statement->bind_param ("s", $username);

			// esecuzione della query
			$statement->execute ();
		
			// definizione di un oggetto con le informazioni dell'utente
			$user = $statement->get_result ()->fetch_assoc ();

			// chiusura dello statement
			$statement->close ();

            // definizione del ruolo da passare
            $user['role'] = $role;
            
			// ritorno dell'oggetto con le informazioni dell'utente 
			return $user;
            
		} else {
			
			return false;
		}
	}

	
	// Metodo per la creazione di una nuova attività e il suo salvataggio nel database
	public function store_tour ($name, $description, $cicerone, $cost, $start_date, $end_date, $locations) {
		
		// acquisizione dell'id del cicerone dal db
		$statement = $this->connection->prepare ("SELECT id FROM cicerone WHERE user_id = ?");
		
		// binding dei dati da inserire nella query
		$statement->bind_param ("s", $cicerone); 

		// esecuzione della query
		$statement->execute ();
		
		// definizione dell'id del cicerone
		$cicerone_id = $statement->get_result()->fetch_assoc()['id'];
		
		// chiusura dello statement
		$statement->close();
		
		// definizione della query di salvataggio della nuova attività
		$statement = $this->connection->prepare ("INSERT INTO tour (name, description, cicerone_id, cost, start_date, end_date) 
					VALUES (?, ?, ?, ?, ?, ?)");
					
		// binding dei dati da inserire nella query
		$statement->bind_param ("ssssss", $name, $description, $cicerone_id, $cost, $start_date, $end_date);

		// esecuzione della query
		$result = $statement->execute ();

		// chiusura dello statement
		$statement->close ();
		
		// se la query va a buon fine
		if($result) {

			// definizione della query di acquisizione del id dell'attività
			$statement = $this->connection->prepare ("SELECT id FROM tour WHERE id=(SELECT MAX(id) FROM tour WHERE cicerone_id = ?)");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $cicerone_id);
			
			// esecuzione della query
			$statement->execute ();
				
			// definisce una nuova variabile con le informazioni dell'attività acquisita
			$tour_id = $statement->get_result()->fetch_assoc()['id'];
				
			// chiusura dello statement
			$statement->close();
				
			foreach($locations as $location) {
				
				// definizione della query di salvataggio della nuova tappa
				$statement = $this->connection->prepare ("INSERT INTO location (tour_id, latitude, longitude) VALUES (?, ?, ?)");
					
				// binding dei parametri da inserire nella query
				$statement->bind_param ("sss", $tour_id, $location[0], $location[1]);
					
				// esecuzione della query
				$result = $statement->execute ();

				// chiusura dello statement
				$statement->close ();
			}
			
		} else {
			
			return false;
		}
		
		return true;
	}


	// Metodo per la creazione di una nuova richiesta di inserimento all'evento e il suo salvataggio nel database
	public function store_request ($globetrotter_username, $event_id, $group_dimension, $created_on) {
		
		// acquisizione dell'id del cicerone dal db
		$statement = $this->connection->prepare ("SELECT id FROM globetrotter WHERE user_id = ?");
		
		// binding dei dati da inserire nella query
		$statement->bind_param ("s", $globetrotter_username); 

		// esecuzione della query
		$statement->execute ();
		
		// definizione dell'id del cicerone
		$globetrotter_id = $statement->get_result()->fetch_assoc()['id'];
		
		// chiusura dello statement
		$statement->close();
		
		// definizione della query di salvataggio della nuova attività
		$statement = $this->connection->prepare ("INSERT INTO request (globetrotter_id, event_id, group_dimension, created_on, accepted) 
					VALUES (?, ?, ?, ?, NULL)");
					
		// binding dei dati da inserire nella query
		$statement->bind_param ("ssss", $globetrotter_id, $event_id, $group_dimension, $created_on);

		// esecuzione della query
		$result = $statement->execute ();

		// chiusura dello statement
		$statement->close ();
		
		// se la query va a buon fine
		if($result) {
		
			return true;
		
		} else {
			
			return false;
		}
	}


	// Metodo per la creazione di una nuova notifica e il suo salvataggio nel database
	public function store_notification ($sender_username, $receiver_username, $reason_id, $created_on, $event_id, $description) {
		
		if($description == "NULL") {
			
			// definizione della query di salvataggio della nuova notifica
			$statement = $this->connection->prepare ("INSERT INTO notification (sender_id, receiver_id, reason_id, 
						created_on, description, event_id) VALUES (?, ?, ?, ?, NULL, ?)");
						
			// binding dei dati da inserire nella query
			$statement->bind_param ("sssss", $sender_username, $receiver_username, $reason_id, $created_on, $event_id);
		
		} else {
			
			// definizione della query di salvataggio della nuova notifica
			$statement = $this->connection->prepare ("INSERT INTO notification (sender_id, receiver_id, reason_id, 
						created_on, description, event_id) VALUES (?, ?, ?, ?, ?, ?)");
						
			// binding dei dati da inserire nella query
			$statement->bind_param ("ssssss", $sender_username, $receiver_username, $reason_id, $created_on, $description, $event_id);
		}
		
		// esecuzione della query
		$result = $statement->execute ();

		// chiusura dello statement
		$statement->close ();
		
		// se la query va a buon fine
		if($result) {
		
			return true;
		
		} else {
			
			return false;
		}
	}


	// Metodo per la creazione di un nuovo evento e il suo salvataggio nel database
	public function store_event ($tour_id, $description, $max_subscribed, $start_date, $end_date) {
		
		if($description == "NULL") {
			
			// definizione della query di salvataggio della nuova notifica
			$statement = $this->connection->prepare ("INSERT INTO event (tour_id, start_date, end_date, 
						description, max_subscribed) VALUES (?, ?, ?, NULL, ?)");
						
			// binding dei dati da inserire nella query
			$statement->bind_param ("ssss", $tour_id, $start_date, $end_date, $max_subscribed);
		
		} else {
			
			// definizione della query di salvataggio della nuova notifica
			$statement = $this->connection->prepare ("INSERT INTO event (tour_id, start_date, end_date, 
						description, max_subscribed) VALUES (?, ?, ?, ?, ?)");
						
			// binding dei dati da inserire nella query
			$statement->bind_param ("sssss", $tour_id, $start_date, $end_date, $description, $max_subscribed);
		}
		
		// esecuzione della query
		$result = $statement->execute ();
		
		$id = $statement->insert_id;

		// chiusura dello statement
		$statement->close ();
		
		// se la query va a buon fine
		if($result) {
		
			return $id;
		
		} else {
			
			return false;
		}
	}


	// Metodo per la creazione di un collegamento tra lingua ed evento e il suo salvataggio nel database
	public function store_event_spoken_language ($event_id, $language_id) {
		
		// definizione della query di salvataggio della nuova notifica
		$statement = $this->connection->prepare ("INSERT INTO event_spoken_language (event_id, language_id)
					VALUES (?, ?)");
						
		// binding dei dati da inserire nella query
		$statement->bind_param ("ss", $event_id, $language_id);
		
		// esecuzione della query
		$result = $statement->execute ();

		// chiusura dello statement
		$statement->close ();
		
		// se la query va a buon fine
		if($result) {
		
			return false;
		
		} else {
			
			return false;
		}
	}
	
	
	// Metodo per la creazione di una nuova tappa dell'attività
	public function store_location ($tour_id, $latitude, $longitude) {
		
		// definizione della query di salvataggio della nuova tappa
		$statement = $this->connection->prepare ("INSERT INTO location (tour_id, latitude, longitude) VALUES (?, ?, ?)");
						
		// binding dei dati da inserire nella query
		$statement->bind_param ("sss", $tour_id, $latitude, $longitude);
		
		// esecuzione della query
		$result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close ();
		
		// se la query va a buon fine
		if($result) {
		
			return true;
		
		} else {
			
			return false;
		}
	}
	
	
	// Metodo per la creazione di una risposta alla richiesta
	public function store_request_response ($globetrotter, $event_id, $group_dimension, $is_accepted) {
		
		// acquisizione dell'id del cicerone dal db
		$statement = $this->connection->prepare ("SELECT id FROM globetrotter WHERE user_id = ?");
		
		// binding dei dati da inserire nella query
		$statement->bind_param ("s", $globetrotter); 

		// esecuzione della query
		$statement->execute ();
		
		// definizione dell'id del cicerone
		$globetrotter_id = $statement->get_result()->fetch_assoc()['id'];
		
		// chiusura dello statement
		$statement->close();
		
		// se la richiesta è stata accettata allora inserisci una nuova iscrizione
		if($is_accepted == "TRUE") {
			
			// definizione della query di salvataggio della nuova iscrizione
			$statement = $this->connection->prepare ("INSERT INTO subscribed (globetrotter_id, event_id, 
						group_dimension, has_payed) VALUES (?, ?, ?, 0)");
						
			// binding dei dati da inserire nella query
			$statement->bind_param ("sss", $globetrotter_id, $event_id, $group_dimension);
			
			// esecuzione della query
			$result = $statement->execute ();

			// chiusura dello statement
			$statement->close ();
			
			// chiusura se la query non va a buon fine
			if(!$result)
				return false;
			
			// definizione della query di aggiornamento della richiesta
			$statement = $this->connection->prepare ("UPDATE request 
						SET accepted = 1 
						WHERE globetrotter_id = ? AND event_id = ?");
			
			// binding dei dati da inserire nella query
			$statement->bind_param ("ss", $globetrotter_id, $event_id);
						
			// esecuzione della query
			$result = $statement->execute ();

			// chiusura dello statement
			$statement->close ();
		
		} else {
			
			// definizione della query di aggiornamento della richiesta
			$statement = $this->connection->prepare ("UPDATE request 
						SET accepted = 0 
						WHERE globetrotter_id = ? AND event_id = ?");
			
			// binding dei dati da inserire nella query
			$statement->bind_param ("ss", $globetrotter_id, $event_id);
						
			// esecuzione della query
			$result = $statement->execute ();

			// chiusura dello statement
			$statement->close ();
		}
		
		// se la query va a buon fine
		if($result) {
		
			return true;
		
		} else {
			
			return false;
		}
	}
	
	
	// Metodo per la creazione delle recensioni
	public function store_feedback ($globetrotter, $tour_id, $rate, $description, $created_on) {
	
		// acquisizione dell'id del cicerone dal db
		$statement = $this->connection->prepare ("SELECT id FROM globetrotter WHERE user_id = ?");
		
		// binding dei dati da inserire nella query
		$statement->bind_param ("s", $globetrotter); 

		// esecuzione della query
		$statement->execute ();
		
		// definizione dell'id del cicerone
		$globetrotter_id = $statement->get_result()->fetch_assoc()['id'];
		
		// chiusura dello statement
		$statement->close();
		
		
		// cancellazione dei feedback pregressi del globetrotter su un certo tour
		$statement = $this->connection->prepare ("DELETE FROM feedback WHERE globetrotter_id = ? AND tour_id = ?");
		
		// binding dei dati da inserire nella query
		$statement->bind_param ("ss", $globetrotter_id, $tour_id); 

		// esecuzione della query
		$statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		
		// salvataggio del feedback del globetrotter su un certo tour
		$statement = $this->connection->prepare ("INSERT INTO feedback (globetrotter_id, tour_id, rate, description, 
							created_on) VALUES (?, ?, ?, ?, ?)");
		
		// binding dei dati da inserire nella query
		$statement->bind_param ("sssss", $globetrotter_id, $tour_id, $rate, $description, $created_on); 

		// esecuzione della query
		$result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// se la query va a buon fine
		if($result) {
		
			return true;
		
		} else {
			
			return false;
		}
	}
		
	

	// ------ FUNZIONI DI ACQUISIZIONE ------

	// Metodo per l'acquisizione dell'utente tramite email e password		
	public function get_user ($email, $password) {
	
		// definizione della query di acquisizione dell'utente
		$statement = $this->connection->prepare ("SELECT * FROM user WHERE email = ?");

		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $email);

		// se si ha un risultato		
		if ($statement->execute ()) {
			
			// definisce un nuovo oggetto con le informazioni dell'utente acquisito
			$user = $statement->get_result ()->fetch_assoc ();

			// chiusura dello statement
			$statement->close ();

			// acquisizione della password criptata
		    $encrypted_password = password_hash($password, PASSWORD_DEFAULT);

			// decriptazione e verifica della password per ritornare l'utente
			if (password_verify ($password, $user['password'])) {
                
                // definizione della query di acquisizione del ruolo dell'utente
                $statement = $this->connection->prepare ("SELECT * FROM cicerone WHERE user_id = ?");

                // binding dei parametri da inserire nella query
                $statement->bind_param ("s", $user['username']);
                
                // esecuzione della query
                $statement->execute ();
	
                // salvataggio dei dati per valutarne le righe acquisite
                $statement->store_result ();

                // se e' stato acquisito qualcosa allora è un cicerone
                if ($statement->num_rows > 0) 
                    $user['role'] = 1; 
                else 
                    $user['role'] = 0;
                
                // chiusura dello statement
                $statement->close ();
                    
                // restituzione dell'utente
                return $user;
            }
				

		} else {
		
			return null;			
		}
	}

    
	// Metodo per l'acquisizione delle lingue parlate da una certa persona
	public function get_user_spoken_languages ($username) {

    
        // definizione della query di acquisizione dell'utente
        $statement = $this->connection->prepare ("SELECT * FROM user_spoken_language WHERE username_id = ?");

        // binding dei parametri da inserire nella query
        $statement->bind_param ("s", $username);

        // se si ha un risultato
        if ($statement->execute ()) {

            // definizione della variabile delle lingue
            $languages = null;

            // definisce un nuovo oggetto con le informazioni dell'utente acquisito
            $result = $statement->get_result();

            // acquisizione di ogni linguaggio recuperato dalla query
            while($language = $result->fetch_array()){

                $languages[] = array(
                    "username_id" => $language['username_id'],
                    "language_id" => $language['language_id']
                ); 
            }

            // chiusura dello statement
            $statement->close ();

            // ritorna il risultato acquisito
            return $languages;

        } else {

            return null;
        }
    }


	// metodo per l'acquisizione delle lingue	
	public function get_languages () {

	    // definizione della query di acquisizione delle lingue
    	$statement = $this->connection->prepare ("SELECT * FROM language");
    		
    	// se si ha un risultato
    	if ($statement->execute ()) {

            // definizione della variabile delle lingue
		    $languages = null;

		    // definisce un nuovo oggetto con le informazioni dell'utente acquisito
		    $result = $statement->get_result();

            // acquisizione di ogni linguaggio recuperato dalla query
		    while($language = $result->fetch_array()){

		        $languages[] = array(
                    "id" => $language['id'],
                    "name" => $language['name']
                ); 
		    }

    		// chiusura dello statement
    		$statement->close ();

            // ritorna il risultato acquisito
            return $languages;

    	} else {

	  		return null;
    	}
	}


	// metodo per l'acquisizione delle lingue	
	public function get_languages_from_event ($id) {

	    // definizione della query di acquisizione delle lingue
    	$statement = $this->connection->prepare ("SELECT * FROM event_spoken_language esl, language l 
									WHERE esl.language_id = l.id  AND esl.event_id = ?");
        $statement->bind_param ("s", $id);
	
    	// se si ha un risultato
    	if ($statement->execute ()) {

            // definizione della variabile delle lingue
		    $languages = null;

		    // definisce un nuovo oggetto con le informazioni dell'utente acquisito
		    $result = $statement->get_result();

            // acquisizione di ogni linguaggio recuperato dalla query
		    while($language = $result->fetch_array()){

		        $languages[] = array(
                    "id" => $language['id'],
                    "name" => $language['name']
                ); 
		    }

    		// chiusura dello statement
    		$statement->close ();

            // ritorna il risultato acquisito
            return $languages;

    	} else {

	  		return null;
    	}
	}


	// metodo per l'acquisizione di un insieme di attività legate ad un cicerone
	public function get_tours ($username, $is_a_cicerone) {
		
		// se si desiderano le attività del cicerone
		if($is_a_cicerone == 1) {
			
			// se si desiderano attività attive oppure no, la query cambia in base al valore di end_date
			$statement = $this->connection->prepare ("SELECT * FROM tour 
										WHERE cicerone_id = ( SELECT id from cicerone where user_id = ? ) AND start_date != '0000-00-00'");
		
			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $username);
			
			// se si ha un risultato
			if ($statement->execute ()) {

				// definizione della variabile delle attività
				$tours = null;

				// definisce un nuovo oggetto con le informazioni dell'utente acquisito
				$result = $statement->get_result();

				// acquisizione di ogni attività recuperata dalla query
				while($tour = $result->fetch_array()){

					$tours[] = array(
						"id" => $tour['id'],
						"name" => $tour['name'],
						"description" => $tour['description'],
						"cicerone_id" => $tour['cicerone_id'],
						"cost" => $tour['cost'],
						"start_date" => $tour['start_date'],
						"end_date" => $tour['end_date']
					); 
				}

				// chiusura dello statement
				$statement->close ();

				// ritorna il risultato acquisito
				return $tours;

			} else {

				return null;
			}
		
		} else if ($is_a_cicerone == 0) {
			
			// se si desiderano attività attive oppure no, la query cambia in base al valore di end_date
			$statement = $this->connection->prepare ("SELECT tou.id, tou.name, tou.description, tou.description, 
											tou.cicerone_id, tou.cost, tou.start_date, tou.end_date
										FROM ((subscribed sub LEFT JOIN event ev ON sub.event_id = ev.id) 
											LEFT JOIN tour tou ON ev.tour_id = tou.id)
											LEFT JOIN globetrotter glob ON sub.globetrotter_id = glob.id
										WHERE glob.user_id = ? AND tou.start_date != '0000-00-00'");
		
			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $username);
			
			// se si ha un risultato
			if ($statement->execute ()) {

				// definizione della variabile delle attività
				$tours = null;

				// definisce un nuovo oggetto con le informazioni dell'utente acquisito
				$result = $statement->get_result();

				// acquisizione di ogni attività recuperata dalla query
				while($tour = $result->fetch_array()){

					$tours[] = array(
						"id" => $tour['id'],
						"name" => $tour['name'],
						"description" => $tour['description'],
						"cicerone_id" => $tour['cicerone_id'],
						"cost" => $tour['cost'],
						"start_date" => $tour['start_date'],
						"end_date" => $tour['end_date']
					); 
				}

				// chiusura dello statement
				$statement->close ();

				// ritorna il risultato acquisito
				return $tours;

			} else {

				return null;
			}
		}
	}
	

	// metodo per l'acquisizione degli eventi del globetrotter
    public function get_events_subscribed ($username){
              
		// definizione dell query
        $statement = $this->connection->prepare ("SELECT gl.user_id, sub.globetrotter_id, sub.event_id, ev.start_date, 
											ev.end_date, ev.description,sub.group_dimension, ev.max_subscribed, 
											t.cost AS tour_cost, t.name AS tour_name, t.id AS tour_id
                                        FROM cicerone.subscribed sub, cicerone.event ev, tour t, globetrotter gl
                                        WHERE sub.event_id = ev.id
                                            AND ev.tour_id = t.id
                                            AND sub.globetrotter_id = gl.id
                                            AND gl.user_id = ?");
												
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $username);
			
		// se si ha un risultato
		if ($statement->execute ()) {

			// definizione della variabile delle attività
			$tours = null;

			// definisce un nuovo oggetto con le informazioni dell'utente acquisito
			$result = $statement->get_result();

			// acquisizione di ogni attività recuperata dalla query
			while($tour = $result->fetch_array()){

				$tours[] = array(
					"globetrotter_id" => $tour['globetrotter_id'],
					"event_id" => $tour['event_id'],
					"start_date" => $tour['start_date'],
					"end_date" => $tour['end_date'],
                    "description" => $tour['description'],
					"group_dimension" => $tour['group_dimension'],
					"max_subscribed" => $tour['max_subscribed'],
					"tour_cost" => $tour['tour_cost'],
                    "tour_name" => $tour['tour_name'],
                    "tour_id" => $tour['tour_id']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $tours;
        
		} else {

			return null;
		}
    }
    
	
	// metodo per l'acquisizione di un'attività legata ad un certo id
	public function get_tour ($tour_id) {
		
		// se si desiderano attività attive oppure no, la query cambia in base al valore di end_date
		$statement = $this->connection->prepare ("SELECT * FROM tour WHERE id = ? ");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un risultato
		if ($statement->execute ()) {

			// acquisizione del cicerone
			$tour = $statement->get_result()->fetch_assoc();

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $tour;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione di una lista di attività
	// in base ad una ricerca effettuata
	public function get_searched_tours ($keywords, $my_username) {
		
		// suddivisione della query inserita in diverse keyword
		$keyword_list = explode(" ", $keywords);
		
		$formatted_keyword_list = "";
		$i = 0;
		
		// inserimento dei simboli % prima e dopo la keyword
		foreach ($keyword_list as $keyword) {
		
			if(strlen($keyword) > 2){
				
				$formatted_keyword_list .= $keyword;
				$i++;
			
				if($i < sizeof($keyword_list))
					$formatted_keyword_list .= "|";
			}
		}
		
		echo "Keywords: " .$formatted_keyword_list;
		
		if(strcmp($formatted_keyword_list, "") !== 0){
			
			// se si desiderano attività attive oppure no, la query cambia in base al valore di end_date
			$statement = $this->connection->prepare ("SELECT t.id, t.name, t.description, t.cicerone_id, t.cost, t.start_date, t.end_date  	 
											FROM tour t LEFT JOIN cicerone c ON t.cicerone_id = c.id
											WHERE (t.name REGEXP ? OR t.description REGEXP ? OR c.user_id REGEXP ?)
											AND c.user_id != ? AND start_date != '0000-00-00'");
			
			// binding dei parametri da inserire nella query
			$statement->bind_param ("ssss", $formatted_keyword_list, $formatted_keyword_list, $formatted_keyword_list, $my_username);
			
			// se si ha un risultato
			if ($statement->execute ()) {

				// definizione della variabile delle attività
				$tours = null;

				// definisce un nuovo oggetto con le informazioni dei tour acquisiti
				$result = $statement->get_result();

				// acquisizione di ogni attività recuperata dalla query
				while($tour = $result->fetch_array()){

					$tours[] = array(
						"id" => $tour['id'],
						"name" => $tour['name'],
						"description" => $tour['description'],
						"cicerone_id" => $tour['cicerone_id'],
						"cost" => $tour['cost'],
						"start_date" => $tour['start_date'],
						"end_date" => $tour['end_date'],
					); 
				}

				// chiusura dello statement
				$statement->close ();

				// ritorna il risultato acquisito
				return $tours;

			} else {

				return null;
			}
			
		} else {
			
			return null;
		}
	}
	
	
	// metodo per l'acquisizione di una lista di notifiche
	public function get_notifications ($username) {
		
		// definizione della query
		$statement = $this->connection->prepare ("SELECT n.id AS id, n.description AS description, 
											n.created_on AS created_on, r.name AS reason_name, t.id AS tour_id,
											e.description AS event_description, e.start_date AS event_start_date, 
											e.end_date AS event_end_date, e.id AS event_id, t.name AS tour_name,
											u.username AS sender_username, u.image_path AS image_path
										FROM (((notification n LEFT JOIN reason r ON n.reason_id = r.id)
											LEFT JOIN event e ON e.id = n.event_id) 
											LEFT JOIN tour t ON t.id = e.tour_id)
											LEFT JOIN user u ON n.sender_id = u.username
										WHERE n.receiver_id = ?");
										
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $username);								
			
		// se si ha un risultato
		if ($statement->execute ()) {

			// definizione della variabile delle notifiche
			$notifications = null;

			// definisce un nuovo oggetto con le informazioni delle notifiche acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni notifica recuperata dalla query
			while($data = $result->fetch_array()){

				$notifications[] = array(
					"id" => $data['id'],
					"description" => $data['description'],
					"tour_name" => $data['tour_name'],
					"event_description" => $data['event_description'],
					"event_start_date" => $data['event_start_date'],
					"event_end_date" => $data['event_end_date'],
					"event_id" => $data['event_id'],
					"tour_id" => $data['tour_id'],
					"reason_name" => $data['reason_name'],
					"created_on" => $data['created_on'],
					"sender_username" => $data['sender_username'],
					"image_path" => $data['image_path']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $notifications;

		} else {

			return null;
		}				
	}
    
	
	// metodo per l'acquisizione del cicerone in base all'id
	public function get_cicerone ($cicerone_id) {
		
		// definizione della query
		$statement = $this->connection->prepare ("SELECT u.username, u.email, u.first_name, 
									u.last_name, u.image_path, u.phone, u.biography
								FROM cicerone c left join user u on c.user_id = u.username
								WHERE id = ?");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $cicerone_id);
			
			
		// se si ha un risultato
		if ($statement->execute ()) {

			// acquisizione del cicerone
			$user = $statement->get_result()->fetch_assoc();

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $user;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione delle tappe in base all'attività associate
	public function get_tour_locations ($tour_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT * FROM location WHERE tour_id = ?");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un risultato
		if ($statement->execute ()) {

			// definizione della variabile delle tappe
			$locations = null;

			// definisce un nuovo oggetto con le informazioni delle tappe acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni tappa recuperata dalla query
			while($locations = $result->fetch_array()){

				$location_list[] = array(
					"latitude" => $locations['latitude'],
					"longitude" => $locations['longitude']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $location_list;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione degli eventi in base all'attività associata
	public function get_events ($tour_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT * FROM event WHERE tour_id = ? AND start_date != '0000-00-00'");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un1 risultato
		if ($statement->execute ()) {

			// definizione della variabile degli eventi
			$events = null;

			// definisce un nuovo oggetto con le informazioni degli eventi acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni evento recuperata dalla query
			while($events = $result->fetch_array()){

				$event_list[] = array(
					"id" => $events['id'],
					"tour_id" => $events['tour_id'],
					"start_date" => $events['start_date'],
					"end_date" => $events['end_date'],
					"description" => $events['description'],
					"max_subscribed" => $events['max_subscribed']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $event_list;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione dell'evento in base all'id associato
	public function get_event_by_id ($event_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT * FROM event WHERE id = ?");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $event_id);
			
		// se si ha un1 risultato
		if ($statement->execute ()) {

			// definisce un nuovo oggetto con le informazioni dell'evento acquisito
			$event = $statement->get_result()->fetch_assoc();

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $event;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione delle recensioni in base all'attività associata
	public function get_feedbacks ($tour_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT f.id, f.rate, f.description, f.created_on, 
									u.username, u.email, u.first_name, u.last_name, u.image_path, 
									u.phone, u.biography
								FROM (feedback f LEFT JOIN globetrotter g ON f.globetrotter_id = g.id)
									LEFT JOIN user u ON g.user_id = u.username
								WHERE f.tour_id = ?");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un1 risultato
		if ($statement->execute ()) {

			// definizione della variabile delle recensioni
			$feedbacks = null;

			// definisce un nuovo oggetto con le informazioni delle recensioni acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni recensione recuperata dalla query
			while($feedbacks = $result->fetch_array()){

				$feedback_list[] = array(
					"id" => $feedbacks['id'],
					"rate" => $feedbacks['rate'],
					"description" => $feedbacks['description'],
					"created_on" => $feedbacks['created_on'],
					"username" => $feedbacks['username'],
					"email" => $feedbacks['email'],
					"first_name" => $feedbacks['first_name'],
					"last_name" => $feedbacks['last_name'],
					"image_path" => $feedbacks['image_path'],
					"phone" => $feedbacks['phone'],
					"biography" => $feedbacks['biography']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $feedback_list;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione delle lingue parlate nel complesso nell'attività associata
	public function get_tour_spoken_languages ($tour_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT l.name, l.id as language_id, e.id as event_id
									FROM ((event_spoken_language es LEFT JOIN event e ON es.event_id = e.id)
										LEFT JOIN language l ON l.id = es.language_id)
									WHERE e.tour_id =?");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un1 risultato
		if ($statement->execute ()) {

			// definizione della variabile delle lingue parlate
			$languages = null;

			// definisce un nuovo oggetto con le informazioni delle lingue acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni recensione recuperata dalla query
			while($languages = $result->fetch_array()){

				$language_list[] = array(
					"language_id" => $languages['language_id'],
					"name" => $languages['name'],
					"event_id" => $languages['event_id'],
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $language_list;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione delle iscrizioni alle attività associate
	public function get_tour_subscribeds ($tour_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT e.id as event_id, s.group_dimension, s.has_payed,
											u.username, u.email, u.first_name, u.last_name, u.image_path,
											u.phone, u.biography
										FROM ((subscribed s LEFT JOIN event e ON s.event_id = e.id)
											LEFT JOIN globetrotter g ON s.globetrotter_id = g.id)
											LEFT JOIN user u ON u.username = g.user_id
										WHERE e.tour_id = ?");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un1 risultato
		if ($statement->execute ()) {

			// definizione della variabile delle iscrizioni
			$subscribeds = null;

			// definisce un nuovo oggetto con le informazioni delle iscrizioni acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni iscrizione recuperata dalla query
			while($subscribeds = $result->fetch_array()){

				$subscribed_list[] = array(
					"event_id" => $subscribeds['event_id'],
					"group_dimension" => $subscribeds['group_dimension'],
					"has_payed" => $subscribeds['has_payed'],
					"username" => $subscribeds['username'],
					"email" => $subscribeds['email'],
					"first_name" => $subscribeds['first_name'],
					"last_name" => $subscribeds['last_name'],
					"image_path" => $subscribeds['image_path'],
					"phone" => $subscribeds['phone'],
					"biography" => $subscribeds['biography']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $subscribed_list;

		} else {

			return null;
		}
	}
	
	
	// metodo per l'acquisizione delle richieste alle attività associate
	public function get_tour_requests ($tour_id) {
		
		// definizione della query		
		$statement = $this->connection->prepare ("SELECT r.event_id, r.group_dimension,
											r.created_on, r.accepted, u.username, u.email, u.first_name, 
											u.last_name, u.image_path, u.phone, u.biography
										FROM (((request r LEFT JOIN globetrotter g ON r.globetrotter_id = g.id)
											LEFT JOIN user u ON g.user_id = u.username)
											LEFT JOIN event e ON r.event_id = e.id)
										WHERE e.tour_id = ? AND r.accepted IS NULL");
		
		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $tour_id);
			
		// se si ha un1 risultato
		if ($statement->execute ()) {

			// definizione della variabile delle richieste
			$requests = null;

			// definisce un nuovo oggetto con le informazioni delle richieste acquisite
			$result = $statement->get_result();
			
			// acquisizione di ogni richiesta recuperata dalla query
			while($requests = $result->fetch_array()){

				$request_list[] = array(
					"event_id" => $requests['event_id'],
					"group_dimension" => $requests['group_dimension'],
					"created_on" => $requests['created_on'],
					"accepted" => $requests['accepted'],
					"username" => $requests['username'],
					"email" => $requests['email'],
					"first_name" => $requests['first_name'],
					"last_name" => $requests['last_name'],
					"image_path" => $requests['image_path'],
					"phone" => $requests['phone'],
					"biography" => $requests['biography']
				); 
			}

			// chiusura dello statement
			$statement->close ();

			// ritorna il risultato acquisito
			return $request_list;

		} else {

			return null;
		}
	}
	
	
		
    // ------- FUNZIONI DI AGGIORNAMENTO -------
    
    // Metodo per la modifica dei dati personali dell'utente
    public function update_user_infos ($username, $password, $email, $first_name, $last_name, $phone_number, $biography) {
        
        
        if ($password == null) {
            
            // definizione della query di acquisizione dell'utente
            $statement = $this->connection->prepare ("UPDATE user 
                                                      SET email = ?, 
                                                        first_name = ?,
                                                        last_name = ?,
                                                        phone = ?,
                                                        biography = ?,
                                                        image_path = ?
                                                      WHERE username = ?");

            $image_dir = "images/" .$username .".jpg";

            // binding dei parametri da inserire nella query
            $statement->bind_param ("sssssss", $email, $first_name, $last_name, $phone_number, 
                                    $biography, $image_dir, $username);


            // esecuzione della query
            $result = $statement->execute ();

            if ($result) {

                /// definizione della query di acquisizione dei dati inseriti
                $statement = $this->connection->prepare ("SELECT * FROM user WHERE username = ?");

                // binding dei dati da inserire nella query
                $statement->bind_param ("s", $username);

                // esecuzione della query
                $statement->execute ();

                // definizione di un oggetto con le informazioni dell'utente
                $user = $statement->get_result ()->fetch_assoc ();

                // chiusura dello statement
                $statement->close ();

                // ritorno dell'oggetto con le informazioni dell'utente 
                return $user;

            } else {

                return false;
            }
            
        } else {
            
            // criptazione della password fornita
            $encrypted_password = password_hash($password, PASSWORD_DEFAULT);
            
            // definizione della query di acquisizione dell'utente
            $statement = $this->connection->prepare ("UPDATE user 
                                                      SET password = ?
                                                      WHERE username = ?");
            
            // binding dei parametri da inserire nella query
            $statement->bind_param ("ss", $encrypted_password, $username);


            // esecuzione della query
            $result = $statement->execute ();
            
            if ($result) {

                /// definizione della query di acquisizione dei dati inseriti
                $statement = $this->connection->prepare ("SELECT * FROM user WHERE username = ?");

                // binding dei dati da inserire nella query
                $statement->bind_param ("s", $username);

                // esecuzione della query
                $statement->execute ();

                // definizione di un oggetto con le informazioni dell'utente
                $user = $statement->get_result ()->fetch_assoc ();

                // chiusura dello statement
                $statement->close ();

                // ritorno dell'oggetto con le informazioni dell'utente 
                return $user;

            } else {

                return false;
            }
            
        }
    }
    
      
    // Metodo per la modifica dei dati personali dell'utente
    public function update_spoken_languages ($username, $spoken_languages) {
        
        $myfile = fopen("newfile.txt", "a") or die("Unable to open file!");
        fwrite($myfile, var_dump($spoken_languages));
        fclose($myfile);
        
        
        foreach ($spoken_languages as $language_id => $language_value) {
        
            // definizione della query di acquisizione delle lingue parlate dall'utente
            $statement = $this->connection->prepare ("SELECT * 
                                                      FROM user_spoken_language 
                                                      WHERE username_id = ? 
                                                        AND language_id = ?");
            
            // binding dei parametri da inserire nella query
            $statement->bind_param ("ss", $username, $language_id);
         
            // esecuzione della query
            $statement->execute ();
	
            // salvataggio dei dati per valutarne le righe acquisite
            $statement->store_result ();
            
            if ($statement->num_rows > 0)
                $exist = true;
            else
                $exist = false;
            
            // chiusura dello statement
            $statement->close();

            // se la lingua gia esiste e il valore booleano passato è falso allora la si vuole cancellare
            if ($exist && $language_value == "false") {
                
                
                // definizione della query di cancellazione della lingua dell'utente
                $statement = $this->connection->prepare ("DELETE FROM user_spoken_language
                                                          WHERE username_id = ? AND
                                                            language_id = ?");
            
                // binding dei parametri da inserire nella query
                $statement->bind_param ("ss", $username, $language_id);
                
                // esecuzione della query
                $result = $statement->execute ();

                // chiusura dello statement
                $statement->close ();
                
                if (!$result)
                    return false;
            } 
            
            // se la lingua non esiste e il valore booleano passato è vero allora la si vuole salvare
            else if (!$exist && $language_value == "true") {

                // chiusura dello statement
                //statement->close();
                
                // definizione della query di inserimento della lingua dell'utente
                $statement = $this->connection->prepare ("INSERT INTO 
                                                            user_spoken_language (username_id, language_id)
                                                          VALUES (?, ?)");
            
                // binding dei parametri da inserire nella query
                $statement->bind_param ("ss", $username, $language_id);
                
                // esecuzione della query
                $result = $statement->execute ();

                // chiusura dello statement
                $statement->close ();
                
                if (!$result)
                    return false;
            }
              
        }
        
        return true;
    }
    
	
	// Metodo per la modifica dei dati dell'attività
    public function update_tour ($id, $name, $description, $cost, $start_date, $end_date) {
        
		// definizione della query di aggiornamento dell'attività
        $statement = $this->connection->prepare ("UPDATE tour 
												  SET name = ?, description = ?, cost = ?,
													start_date = ?, end_date = ?	
                                                  WHERE id = ?");

        // binding dei parametri da inserire nella query
        $statement->bind_param ("ssssss", $name, $description, $cost, $start_date, $end_date, $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		if ($result) {
			
			return true;
			
		} else {
			
			return false;
		}
    }
    
    
	// Metodo per la modifica dei dati dell'evento
    public function update_event ($id, $description, $max_subscription, $start_date, $end_date) {
        
		// se non esiste una descrizione
		if($description == "NULL") {
			
			// definizione della query di aggiornamento dell'attività
			$statement = $this->connection->prepare ("UPDATE event 
													  SET description = NULL, max_subscription = ?, start_date = ?, end_date = ?	
													  WHERE id = ?");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("ssss", $max_subscription, $start_date, $end_date, $id);
		
		} else {

			// definizione della query di aggiornamento dell'attività
			$statement = $this->connection->prepare ("UPDATE event 
													  SET description = ?, max_subscribed = ?, start_date = ?, end_date = ?	
													  WHERE id = ?");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("sssss", $description, $max_subscription, $start_date, $end_date, $id);
		}
		
		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
			
		// se l'esecuzione va a buon fine	
		if ($result) {
		
			// definizione della query di cancellazione delle lingue dell'evento
			$statement = $this->connection->prepare ("DELETE FROM event_spoken_language WHERE event_id = ?");
		
			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $id);
			
			// esecuzione della query
			$statement->execute ();
			
			// chiusura dello statement
			$statement->close();
		
			return true;
			
		} else {
			
			return false;
		}
    }
    
	
	// Metodo per la modifica dei dati delle iscrizioni
    public function update_subscriptions ($username, $is_cicerone) {
        
		// se non esiste una descrizione
		if($is_cicerone == "1") {
			
			// definizione della query di acquisizione delle iscrizioni
			$statement = $this->connection->prepare ("SELECT s.globetrotter_id, s.event_id
								FROM (((subscribed s LEFT JOIN event e ON s.event_id = e.id)
									LEFT JOIN tour t ON e.tour_id = t.id)
									LEFT JOIN cicerone c ON c.id = t.cicerone_id)
								WHERE NOW() >= DATE_SUB(e.start_date, INTERVAL 3 DAY) 
									AND s.has_payed = 0
									AND t.cost > 0
									AND c.user_id = ?");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $username);
		
		} else {

			// definizione della query di aggiornamento dell'attività
			$statement = $this->connection->prepare ("SELECT r.globetrotter_id, r.event_id
								FROM (((request r LEFT JOIN event e ON r.event_id = e.id)
									LEFT JOIN tour t ON e.tour_id = t.id)
									LEFT JOIN globetrotter g ON g.id = r.globetrotter_id)
								WHERE NOW() >= DATE_SUB(e.start_date, INTERVAL 3 DAY) 
									AND r.accepted IS NULL
									AND g.user_id = ?");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $username);
		}
		
		// esecuzione della query
        if($statement->execute()) {
			
			// acquisizione dei risultati
			$lines = $statement->get_result();
		
			// acquisizione di ogni linguaggio recuperato dalla query
            while($line = $lines->fetch_array()){

                // definizione della query di cancellazione dell'iscrizione
				$statement = $this->connection->prepare ("DELETE FROM subscribed WHERE globetrotter_id = ? AND event_id = ?");
				
				// binding dei parametri da inserire nella query
				$statement->bind_param ("ss", $line['globetrotter_id'], $line['event_id']);

				// esecuzione della query
				$result = $statement->execute ();
				
				// chiusura dello statement
				$statement->close();
				
				if(!$result)
					return false;
            }
		}
		
		return true;
    }
    
	
	// Metodo per la modifica dei dati delle richieste di iscrizione
    public function update_requests ($username, $is_cicerone) {
        
		// se non esiste una descrizione
		if($is_cicerone == "1") {
			
			// definizione della query di acquisizione delle iscrizioni
			$statement = $this->connection->prepare ("SELECT r.globetrotter_id, r.event_id
								FROM (((request r LEFT JOIN event e ON r.event_id = e.id)
									LEFT JOIN tour t ON e.tour_id = t.id)
									LEFT JOIN cicerone c ON c.id = t.cicerone_id)
								WHERE NOW() >= DATE_SUB(e.start_date, INTERVAL 3 DAY) 
									AND r.accepted IS NULL
									AND c.user_id = ?");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $username);
		
		} else {

			/// definizione della query di acquisizione delle iscrizioni
			$statement = $this->connection->prepare ("SELECT r.globetrotter_id, r.event_id
								FROM (((request r LEFT JOIN event e ON r.event_id = e.id)
									LEFT JOIN tour t ON e.tour_id = t.id)
									LEFT JOIN cicerone c ON c.id = t.cicerone_id)
								WHERE NOW() >= DATE_SUB(e.start_date, INTERVAL 3 DAY) 
									AND r.accepted IS NULL
									AND c.user_id = ?");

			// binding dei parametri da inserire nella query
			$statement->bind_param ("s", $username);
		}
		
		// esecuzione della query
        if($statement->execute()) {
			
			// acquisizione dei risultati
			$lines = $statement->get_result();
		
			// acquisizione di ogni linguaggio recuperato dalla query
            while($line = $lines->fetch_array()){

                // definizione della query di cancellazione dell'iscrizione
				$statement = $this->connection->prepare ("DELETE FROM request WHERE globetrotter_id = ? AND event_id = ?");
				
				// binding dei parametri da inserire nella query
				$statement->bind_param ("ss", $line['globetrotter_id'], $line['event_id']);

				// esecuzione della query
				$result = $statement->execute ();
				
				// chiusura dello statement
				$statement->close();
				
				if(!$result)
					return false;
            }
		}
		
		return true;
    }


	// Metodo per la modifica dello stato del pagamento dell'iscrizione
    public function update_subscription ($username, $event_id) {
        
		// definizione della query di aggiornamento dell'attività
		$statement = $this->connection->prepare ("SELECT id FROM globetrotter WHERE user_id = ?");

		// binding dei parametri da inserire nella query
		$statement->bind_param ("s", $username);
		
		// esecuzione della query
        if($statement->execute()) {
						
			// acquisizione dei risultati
			$globetrotter_id = $statement->get_result()->fetch_assoc()["id"];

			// chiusura dello statement
			$statement->close();
		
			// definizione della query di cancellazione dell'iscrizione
			$statement = $this->connection->prepare ("UPDATE subscribed SET has_payed = 1 WHERE globetrotter_id = ? AND event_id = ?");
				
			// binding dei parametri da inserire nella query
			$statement->bind_param ("ss", $globetrotter_id, $event_id);

			// esecuzione della query
			$result = $statement->execute ();
			
			// chiusura dello statement
			$statement->close();
				
			if($result)
				return true;
			else
				return false;
            
			
		} else {
			
			return false;
		}
    }
    
    

	
	// ------ FUNZIONI DI CANCELLAZIONE ------
	
	// Metodo per la cancellazione dell'attività
	public function delete_tour ($id) {
	
		// definizione della query di modifica dell'attività
        $statement = $this->connection->prepare ("UPDATE tour SET start_date = NULL, end_date = NULL WHERE id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		
		
		// definizione della query di cancellazione delle tappe dell'attività
        $statement = $this->connection->prepare ("DELETE FROM location WHERE tour_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		
		
		// definizione della query di cancellazione delle recensioni dell'attività
        $statement = $this->connection->prepare ("DELETE FROM feedback WHERE tour_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		else
			return true;
	}
	
	
	// Metodo per la cancellazione dell'evento
	public function delete_event ($id) {
		
		// definizione della query di modifica dell'attività
        $statement = $this->connection->prepare ("UPDATE event SET start_date = NULL, end_date = NULL WHERE id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		
		
		// definizione della query di cancellazione delle lingue parlate nell'evento
        $statement = $this->connection->prepare ("DELETE FROM event_spoken_language WHERE event_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		
		
		// definizione della query di cancellazione delle richieste ricevute per l'evento
        $statement = $this->connection->prepare ("DELETE FROM request WHERE event_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		
		
		// definizione della query di cancellazione delle lingue parlate nell'evento
        $statement = $this->connection->prepare ("DELETE FROM subscribed WHERE event_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della modifica effetutata
		if (!$result) 			
			return false;
		else
			return true;
	}
	
	
	// Metodo per la cancellaizone delle tappe associate all'attività
	public function delete_locations ($tour_id) {
		
		// definizione della query di modifica dell'attività
        $statement = $this->connection->prepare ("DELETE FROM location WHERE tour_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("s", $tour_id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della cancellazione effetutata
		if ($result) 			
			return true;
		else
			return false;
	}
	
	
	// Metodo per la cancellazione dell'iscrizione all'evento
	public function delete_subscription ($globetrotter, $event_id) {
	
		// definizione della query di acquisizione dei dati inseriti
        $statement = $this->connection->prepare ("SELECT id FROM globetrotter WHERE user_id = ?");

        // binding dei dati da inserire nella query
        $statement->bind_param ("s", $globetrotter);

        // esecuzione della query
        $statement->execute ();

        // definizione dell'id del globetrotter della iscrizione
        $globetrotter_id = $statement->get_result ()->fetch_assoc ()['id'];
		
		// chiusura dello statement
		$statement->close();
		
		// definizione della query di cancellazione dell'iscrizione
        $statement = $this->connection->prepare ("DELETE FROM subscribed WHERE globetrotter_id = ? AND event_id = ?");
		
		// binding dei parametri da inserire nella query
        $statement->bind_param ("ss", $globetrotter_id, $event_id);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della cancellazione effetutata
		if ($result) 			
			return true;
		else
			return false;
	}
    
	
	// Metodo per la cancellazione dell'account
	public function delete_my_account ($username) {
	
		// definizione della query di acquisizione dei dati inseriti
        $statement = $this->connection->prepare ("DELETE FROM user WHERE username = ?");

        // binding dei dati da inserire nella query
        $statement->bind_param ("s", $username);

		// esecuzione della query
        $result = $statement->execute ();
		
		// chiusura dello statement
		$statement->close();
		
		// ritorno dell'esito della cancellazione effetutata
		if ($result) 			
			return true;
		else
			return false;
	}
    
	
	
    // ------ FUNZIONI DI SERVIZIO ------

	// Metodo per la verifica di esistenza dello username
	public function is_username_existing ($username) {
	
		// definizione della query per l'acquisizione dell'utente
		$statement = $this->connection->prepare ("SELECT * FROM user WHERE username = ?");

		// binding dei dati da inserire nella query
		$statement->bind_param ("s", $username);

		// esecuzione della query
		$statement->execute ();
	
		// salvataggio dei dati per valutarne le righe acquisite
		$statement->store_result ();

		// se � stato acquisito qualcosa
		if ($statement->num_rows > 0) {

			// allora l'username esiste
			$statement->close ();
			return true;
			
		} else {

			// altrimenti non esiste
			$statement->close ();
			return false;
		}
	}


	// Metodo per la verifica di esistenza della e-mail
	public function is_email_existing ($email) {
	
		// definizione della query per l'acquisizione dell'utente
		$statement = $this->connection->prepare ("SELECT * FROM user WHERE email = ?");

		// binding dei dati da inserire nella query
		$statement->bind_param ("s", $email);

		// esecuzione della query
		$statement->execute ();
	
		// salvataggio dei dati per valutarne le righe acquisite
		$statement->store_result ();

		// se � stato acquisito qualcosa
		if ($statement->num_rows > 0) {

			// allora l'email esiste
			$statement->close ();
			return true;
			
		} else {

			// altrimenti non esiste
			$statement->close ();
			return false;
		}
	}
    
    
    // Metodo per il salvataggio di una nuova password
	public function change_password ($email) {
	
        // caratteri permessi per la creazion della password
        $permitted_chars = '0123456789abcdefghijklmnopqrstuvwxyz';

        // creazione di una stringa casuale
        $password = substr(str_shuffle($permitted_chars), 0, 12);
        
        // criptazione della password fornita
		$encrypted_password = password_hash($password, PASSWORD_DEFAULT);
        
		// definizione della query per l'acquisizione dell'utente
		$statement = $this->connection->prepare ("UPDATE user SET password = ? WHERE email = ? ");

		// binding dei dati da inserire nella query
		$statement->bind_param ("ss", $encrypted_password, $email);

		// esecuzione della query
		$statement->execute ();
	
		// chiusura dello statement
		$statement->close ();

        // ritorna il valore non criptato della password
        return $password;
	}

}

?>