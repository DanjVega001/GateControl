require('dotenv').config();
const express = require('express');
const app = express();
const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const { getFirestore, Timestamp, FieldValue, Filter } = require('firebase-admin/firestore');
const client = require('twilio')(accountSid, authToken);
const axios = require('axios');
const URL_ARDUINO = "http://192.168.1.100/";

const db = getFirestore();

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

app.post('/open-gate', async (req, res) => {

    try {
        const user_id = req.body.user_id;
        const gate_id = req.body.gate_id;

        const userRef = db.collection('users').doc(user_id);
        const gateRef = userRef.collection('gates').doc(gate_id);

        const gate = await gateRef.get();

        if (!gate.exists) {
            throw new Error("Gate not found")
        } 

        setTimeout(() => {
            console.log('Pasaron 3 segundos');
        }, 6000); 

        await gateRef.update({
            "state" : "open"
        }).then((_) => {
            res.status(200).json({
                "message" : "Open gate!"
            })
        }).catch((error) => {
            throw new Error(error.message);
        });


        /*
        const response = await axios.post(URL_ARDUINO+"abrir-porton", gate.data());

        if (response.status==200) {

            await gateRef.update({
                "state" : "open"
            }).then((_) => {
                res.status(200).json({
                    "message" : "Open gate!"
                })
            }).catch((error) => {
                throw new Error(error.message);
            });

        } else {
            throw new Error(response.statusText);
        }
        */
    } catch (error) {
        return res.status(400).json({
            "error" : error
        })
    }

});


app.post('/close-gate', async (req, res) => {

    try {
        const user_id = req.body.user_id;
        const gate_id = req.body.gate_id;

        const userRef = db.collection('users').doc(user_id);
        const gateRef = userRef.collection('gates').doc(gate_id);

        const gate = await gateRef.get();

        if (!gate.exists) {
            throw new Error("Gate not found")
        } 

        setTimeout(() => {
            console.log('Pasaron 3 segundos');
        }, 6000); 

        await gateRef.update({
            "state" : "close"
        }).then((_) => {
            res.status(200).json({
                "message" : "Close gate!"
            })
        }).catch((error) => {
            throw new Error(error.message);
        });

        /*
        const response = await axios.post(URL_ARDUINO+"cerrar-porton", data);

        if (response.status==200) {

            await gateRef.update({
                "state" : "close"
            }).then((_) => {
                res.status(200).json({
                    "message" : "Open gate!"
                })
            }).catch((error) => {
                throw new Error(error.message);
            });

        } else {
            throw new Error(response.statusText);
        }*/

    } catch (error) {
        return res.status(400).json({
            "error" : error
        })
    }

});





module.exports = app;
