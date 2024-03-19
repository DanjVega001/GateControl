const {onRequest} = require("firebase-functions/v2/https");
const admin = require('firebase-admin');
const app = require('./app');

admin.initializeApp();


exports.app = onRequest(app)