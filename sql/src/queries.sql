--Queries for the menu options


--Browse Menu by ItemName
--	with this option, the user are allowed to search by itemName
SELECT *
FROM Menu M
WHERE M.itemName = '_____';

--Browse Menu by Type
--	with this option, the user are allowed to search by type
SELECT * 
FROM Menu M
WHERE M.type = '____';

--Add Order
--	the user can update order
-- 	this gives the order an ID and push the login name and time stamp


--Update Order
--	ask for order ID, show info, check if paid
-- 	if not paid, ask what to change
--	if paid, show error message

--View Current Order
--	ask for order ID, show info

--View Order Status 
--	ask for order ID, go into itemStatus, show all items from that order

--Update User Info
--	show current info, ask what to change

