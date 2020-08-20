<?php

class Connection {

	// variabile per la connessione al database
	private $connection;

	// Metodo per la connessione al database
	public function connect () {

		// caricamento del file di configurazione della connessione	
		require_once 'Config.php';

		// connessione al database mysql
		$this->connection = new mysqli (DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);

		// ritorna la connessione al database effettuata
		return $this->connection;
	}
}

?>