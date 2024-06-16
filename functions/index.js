const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendChatNotification = functions.database.ref('/chats/{torneoId}/{messageId}')
    .onCreate(async (snapshot, context) => {
        try {
            const message = snapshot.val();
            const torneoId = context.params.torneoId;
            const userId = message.userId;
            const messageText = message.message;

            // Obtener tokens FCM de los usuarios inscritos en el torneo
            const snapshotEquipos = await admin.database().ref(`/equipos`).orderByChild('idTorneo').equalTo(torneoId).once('value');
            
            if (!snapshotEquipos.exists()) {
                console.log(`No existen equipos para el torneo ${torneoId}`);
                return null;
            }

            const tokens = [];
            snapshotEquipos.forEach(childSnapshot => {
                const equipo = childSnapshot.val();
                if (equipo.idCapitan !== userId && equipo.tokenFCM) {
                    tokens.push(equipo.tokenFCM);
                }
            });

            // Construir la notificación
            const payload = {
                notification: {
                    title: 'Nuevo mensaje en el torneo',
                    body: `${messageText}`
                }
            };

            // Enviar notificación a los tokens FCM obtenidos
            await admin.messaging().sendToDevice(tokens, payload);
            console.log('Notificación enviada exitosamente a', tokens);
            return null;
        } catch (error) {
            console.error('Error al enviar la notificación:', error);
            return null;
        }
    });