require('dotenv').config();
const express = require('express');
const app = express();
const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const client = require('twilio')(accountSid, authToken);

app.post('/send-number-phone', (req, res) => {
    try {
        var min = 1000;
        var max = 9999;

        var code = Math.floor(Math.random()*(max-min+1)+min)
        const data = req.body;

        var message = "";
        if (data.isLogin) {
            message = "Hemos recibido una solicitud de inicio de sesión en tu cuenta registrado a este número. ";
        }else{
            message = "Hemos recibido la solicitud de activar la autenticacion por dos pasos. ";
        }
        message+="Digita el siguiente codigo para validar esta solicitud "+code;   
        
        client.messages.create({
            body: message,
            from: '+14434867639',
            to: '+57'+data.phone
        }).then(message => console.log(message.sid));

        return res.status(200).json({
            "message" : "SMS SEND",
            "code" : parseInt(code)
        })
    } catch (error) {
        console.log(error);
    }
});



module.exports = app;
