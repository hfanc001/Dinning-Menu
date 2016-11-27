--CREATE LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION order_ID
RETRUNS "trigger" AS 
$BODY$
BEGIN

New.orderid := nextval('orderid_seq');
RETURN New;

END;
$BODY$
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER name BEFORE INSERT
ON Orders FOR EACH ROW
EXECUTE PROCEDURE order_ID();
