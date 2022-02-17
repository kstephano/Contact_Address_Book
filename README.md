# Contact_Address_Book
Simple contact address book project made using Java and Android Studio.

Main features: add, edit and delete contacts saved in Amazon Cloud Firestore; view records uploaded from the database; make phone calls from within the app.

![contact address book 1](https://imgur.com/EcEiTmL.png)
Contacts are displayed in alphabetical order and in sections using a grouped RecyclerView. Contacts can be accessed from this list by clicking on their name,
taking the user to a new page where they can call the contact, edit details, or delete the contact. Upon updating/deleting the contact, the appropriate actions 
are executed to update the Firestore database, and the user is taken back to the Contacts fragment.

![contact address book 1](https://imgur.com/rNNvihh.png)
New contacts are created from the NewContactFragment. Images can be added and uploaded to Firestore Storage. Form validation is present to make sure
user has entered a first name and last name AND phone is not empty and valid OR email is not empty and valid.

