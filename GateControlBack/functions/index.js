const {onRequest} = require("firebase-functions/v2/https");
const admin = require('firebase-admin');

admin.initializeApp();



const app = require('./app');



exports.app = onRequest(app)