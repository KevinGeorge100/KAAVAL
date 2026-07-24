const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * KAAVAL Firebase Cloud Functions
 *
 * 1. On Emergency Activated: Sends Push Notifications to all registered Caregivers.
 * 2. On Caregiver Response: Updates incident state and notifies user app.
 */

exports.onEmergencyActivated = functions.firestore
    .document("incidents/{incidentId}")
    .onCreate(async (snapshot, context) => {
      const incident = snapshot.data();
      const incidentId = context.params.incidentId;

      console.log(`🚨 Emergency Incident ${incidentId} created.`);

      const payload = {
        notification: {
          title: "🚨 KAAVAL EMERGENCY ALERT",
          body: `Emergency SOS triggered by ${incident.userName || "User"}. Tap to open live tracking.`,
        },
        data: {
          incidentId: incidentId,
          trackingUrl: incident.trackingUrl || "",
        },
      };

      const caregiverTokensSnapshot = await admin.firestore()
          .collection("caregiver_tokens")
          .get();

      const tokens = caregiverTokensSnapshot.docs.map((doc) => doc.data().token);

      if (tokens.length > 0) {
        return admin.messaging().sendToDevice(tokens, payload);
      }
      return null;
    });

exports.onCaregiverResponded = functions.firestore
    .document("incidents/{incidentId}/responses/{responseId}")
    .onCreate(async (snapshot, context) => {
      const response = snapshot.data();
      const incidentId = context.params.incidentId;

      return admin.firestore().collection("incidents").doc(incidentId).update({
        status: "ACKNOWLEDGED",
        responderName: response.caregiverName || "Caregiver",
        acknowledgedAt: admin.firestore.FieldValue.serverTimestamp(),
      });
    });
