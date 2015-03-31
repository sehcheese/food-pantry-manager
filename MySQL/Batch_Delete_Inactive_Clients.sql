-- Batch delete inactive clients

-- Make temporary table holding ClientIDs to be deleted
-- This pulls in clients who have not completed an appointment before the date specified near the end of the query
CREATE TEMPORARY TABLE tmp AS (
		SELECT client.client_id FROM `food_pantry_manager`.`appointment`,`food_pantry_manager`.`client` WHERE pounds IS NOT NULL AND appointment.client_id = client.client_id GROUP BY appointment.client_id HAVING MAX(date) < '2013-08-01' ORDER BY MAX(date) ASC
	);

-- For each of the ClientIDs found, remove the records in the client table
-- Per the database setup, these clients' appointments will remain in the appointments table with a null ClientID so that poundage totals will remain correct
DELETE FROM client
 WHERE client_id IN (
       SELECT client_id
         FROM tmp
      );

-- Drop the temporary table
DROP TEMPORARY TABLE tmp;
