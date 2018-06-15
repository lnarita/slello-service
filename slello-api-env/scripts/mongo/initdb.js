db = db.getSiblingDB('slello');

db.createUser( { user: "slello", pwd: "ollels", roles: [ "readWrite", "dbAdmin" ] } );

